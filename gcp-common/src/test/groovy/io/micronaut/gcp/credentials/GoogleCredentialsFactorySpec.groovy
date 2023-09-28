package io.micronaut.gcp.credentials

import com.google.auth.RequestMetadataCallback
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ImpersonatedCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.auth.oauth2.UserCredentials
import com.google.common.util.concurrent.MoreExecutors
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.context.exceptions.ConfigurationException
import io.micronaut.context.exceptions.NoSuchBeanException
import io.micronaut.core.reflect.ReflectionUtils
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.runtime.server.EmbeddedServer
import org.spockframework.runtime.IStandardStreamsListener
import org.spockframework.runtime.StandardStreamsCapturer
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import uk.org.webcompere.systemstubs.properties.SystemProperties
import uk.org.webcompere.systemstubs.resource.Resources

import java.security.PrivateKey

import static io.micronaut.gcp.credentials.fixture.ServiceAccountCredentialsTestHelper.*

class GoogleCredentialsFactorySpec extends Specification {

    SimpleStreamsListener captured = new SimpleStreamsListener()

    @AutoCleanup("stop")
    StandardStreamsCapturer capturer = new StandardStreamsCapturer()

    PollingConditions conditions = new PollingConditions(timeout: 30)

    void setup() {
        capturer.addStandardStreamsListener(captured)
        capturer.start()
    }

    def cleanup() {
        GoogleCredentials gc = GoogleCredentials.getApplicationDefault()
        ReflectionUtils.getFieldValue(GoogleCredentials.class, "defaultCredentialsProvider", gc)
                .ifPresent {
                    ReflectionUtils.setField(it.getClass(), "cachedCredentials", it, null)
                }
    }

    void "GoogleCredentials factory method can be disabled via configuration"() {
        given:
        def ctx = ApplicationContext.run([
                (GoogleCredentialsConfiguration.PREFIX + ".enabled") : false
        ])

        when:
        ctx.getBean(GoogleCredentials)

        then:
        thrown(NoSuchBeanException)
    }

    void "configuring both credentials location and encoded-key throws an exception"() {
        given:
        def ctx = ApplicationContext.run([
                (GoogleCredentialsConfiguration.PREFIX + ".location") : "foo",
                (GoogleCredentialsConfiguration.PREFIX + ".encoded-key") : "bar"
        ])

        when:
        ctx.getBean(GoogleCredentials)

        then:
        def ex = thrown(BeanInstantiationException)
        ex.getCause() instanceof ConfigurationException
    }

    void "default configuration without GCP SDK installed fails"() {
        given:
        SystemProperties props = new SystemProperties()

        URL testHomeDir = this.getClass().getResource("/")
        props.set("user.home", testHomeDir.getPath())
        props.set("os.name", "linux")

        when:
        GoogleCredentials gc = props.execute(() -> {
            def ctx = ApplicationContext.run()
            ctx.getBean(GoogleCredentials)
        })

        then:
        def ex = thrown (BeanInstantiationException)
        ex.getCause() instanceof IOException
        ex.getMessage().contains("Your default credentials were not found.")
    }

    void "user account credentials can be loaded via known environment variable"() {
        given:
        EnvironmentVariables env = new EnvironmentVariables()

        URL testCredentialsFile = this.getClass().getResource("/test-user-account/.config/gcloud/application_default_credentials.json")
        env.set("GOOGLE_APPLICATION_CREDENTIALS", testCredentialsFile.getPath())

        when:
        GoogleCredentials gc = env.execute(() -> {
            def ctx = ApplicationContext.run()
            ctx.getBean(GoogleCredentials)
        })

        then:
        matchesJsonUserCredentials(gc)
    }

    void "user account credentials can be loaded from SDK"() {
        given:
        SystemProperties props = new SystemProperties()

        URL testHomeDir = this.getClass().getResource("/test-user-account")
        props.set("user.home", testHomeDir.getPath())
        props.set("os.name", "linux")

        when:
        GoogleCredentials gc = props.execute(() -> {
            def ctx = ApplicationContext.run()
            ctx.getBean(GoogleCredentials)
        })

        then:
        matchesJsonUserCredentials(gc)
    }

    void "user account credentials can be loaded from SDK on windows"() {
        given:
        EnvironmentVariables env = new EnvironmentVariables()
        SystemProperties props = new SystemProperties()

        URL testAppDataDir = this.getClass().getResource("/test-user-account/.config")
        env.set("APPDATA", testAppDataDir.getPath())
        props.set("os.name", "windows")

        when:
        GoogleCredentials gc = Resources.execute(() -> {
            def ctx = ApplicationContext.run()
            ctx.getBean(GoogleCredentials)
        }, env, props)

        then:
        matchesJsonUserCredentials(gc)
    }

    void "user account credentials can be loaded from custom SDK location"() {
        given:
        EnvironmentVariables env = new EnvironmentVariables()

        URL testGcloudDir = this.getClass().getResource("/test-user-account/.config/gcloud")
        env.set("CLOUDSDK_CONFIG", testGcloudDir.getPath())

        when:
        GoogleCredentials gc = env.execute(() -> {
            def ctx = ApplicationContext.run()
            ctx.getBean(GoogleCredentials)
        })

        then:
        matchesJsonUserCredentials(gc)
    }

    void "impersonated service account credentials can be loaded from SDK"() {
        given:
        SystemProperties props = new SystemProperties()

        URL testHomeDir = this.getClass().getResource("/test-impersonated-service-account")
        props.set("user.home", testHomeDir.getPath())
        props.set("os.name", "linux")

        when:
        GoogleCredentials gc = props.execute(() -> {
            def ctx = ApplicationContext.run()
            ctx.getBean(GoogleCredentials)
        })

        then:
        gc != null
        ImpersonatedCredentials ic = gc.$target
        UserCredentials uc = (UserCredentials) ic.getSourceCredentials()
        ic.getAccount() == "sa-test1@micronaut-gcp-testing.iam.gserviceaccount.com"
        with(uc) {
            getClientId() == "client-id-1.apps.googleusercontent.com"
            getClientSecret() == "client-secret-1"
            getRefreshToken() == "refresh-token-1"
        }
    }

    void "service account credentials can be loaded via environment variable"() {
        given:
        EnvironmentVariables env = new EnvironmentVariables()

        PrivateKey pk = generatePrivateKey()
        File serviceAccountCredentials = writeServiceCredentialsToTempFile(pk)
        env.set("GOOGLE_APPLICATION_CREDENTIALS", serviceAccountCredentials.getPath())

        when:
        GoogleCredentials gc = env.execute(() -> {
            def ctx = ApplicationContext.run()
            ctx.getBean(GoogleCredentials)
        })

        then:
        matchesJsonServiceAccountCredentials(pk, gc)
    }

    void "service account credentials can be loaded via configured location"() {
        given:
        PrivateKey pk = generatePrivateKey()
        File serviceAccountCredentials = writeServiceCredentialsToTempFile(pk)

        when:
        def ctx = ApplicationContext.run([
                (GoogleCredentialsConfiguration.PREFIX + ".location"): serviceAccountCredentials.getPath()
        ])
        GoogleCredentials gc = ctx.getBean(GoogleCredentials)

        then:
        matchesJsonServiceAccountCredentials(pk, gc)
    }

    void "service account credentials can be loaded via configured Base64-encoded key"() {
        given:
        PrivateKey pk = generatePrivateKey()
        String encodedServiceAccountCredentials = encodeServiceCredentials(pk)

        when:
        def ctx = ApplicationContext.run([
                (GoogleCredentialsConfiguration.PREFIX + ".encoded-key"): encodedServiceAccountCredentials
        ])
        GoogleCredentials gc = ctx.getBean(GoogleCredentials)

        then:
        matchesJsonServiceAccountCredentials(pk, gc)
    }

    void "invalid credentials cause a warning to be logged when metadata is requested"(){
        given:
        PrivateKey pk = generatePrivateKey()
        String encodedServiceAccountCredentials = encodeServiceCredentials(pk)
        EmbeddedServer gcp = ApplicationContext.run(EmbeddedServer, [
                "spec.name" : "GoogleCredentialsFactorySpec",
                "micronaut.server.port" : 8080
        ])
        def ctx = ApplicationContext.run([
                (GoogleCredentialsConfiguration.PREFIX + ".encoded-key"): encodedServiceAccountCredentials
        ])
        GoogleCredentials gc = ctx.getBean(GoogleCredentials)

        when:
        gc.getRequestMetadata(null, MoreExecutors.directExecutor(), new RequestMetadataCallback() {
            @Override
            void onSuccess(Map<String, List<String>> metadata) {

            }

            @Override
            void onFailure(Throwable exception) {

            }
        })

        then:
        conditions.eventually {
            captured.messages.any {
                it.contains("WARN")
                it.contains("A failure occurred while attempting to build credential metadata for a GCP API request. The GCP libraries treat this as " +
                        "a retryable error, but misconfigured credentials can keep it from ever succeeding.")
            }
        }

        cleanup:
        ctx.stop()
        gcp.stop()
    }

    private void matchesJsonUserCredentials(GoogleCredentials gc) {
        assert gc != null && gc.$target != null && gc.$target instanceof UserCredentials
        UserCredentials uc = (UserCredentials) gc.$target
        assert uc.getClientId() == "client-id-1.apps.googleusercontent.com"
        assert uc.getClientSecret() == "client-secret-1"
        assert uc.getQuotaProjectId() == "micronaut-gcp-test"
        assert uc.getRefreshToken() == "refresh-token-1"
    }

    private void matchesJsonServiceAccountCredentials(PrivateKey pk, GoogleCredentials gc) {
        assert gc != null && gc.$target != null && gc.$target instanceof ServiceAccountCredentials
        ServiceAccountCredentials sc = (ServiceAccountCredentials) gc.$target
        assert sc.getAccount() == "sa-test1@micronaut-gcp-testing.iam.gserviceaccount.com"
        assert sc.getClientId() == "client-id-1"
        assert sc.getProjectId() == "micronaut-gcp-testing"
        assert sc.getPrivateKeyId() == "private-key-id-1"
        assert sc.getPrivateKey() == pk
    }
}

@Requires(property = "spec.name", value = "GoogleCredentialsFactorySpec")
@Controller
class GoogleAuth {

    @Post(value="/token", processes = MediaType.APPLICATION_FORM_URLENCODED)
    HttpResponse<String> getToken() {
        return HttpResponse.unauthorized()
    }
}

class SimpleStreamsListener implements IStandardStreamsListener {
    List<String> messages = []
    @Override void standardOut(String m) { messages << m }
    @Override void standardErr(String m) { messages << m }
}
