package io.micronaut.gcp.pubsub.bind

import com.google.auth.oauth2.GoogleCredentials
import com.google.pubsub.v1.ProjectSubscriptionName
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.reflect.ReflectionUtils
import io.micronaut.gcp.credentials.GoogleCredentialsConfiguration
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
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

import java.security.PrivateKey

import static io.micronaut.gcp.credentials.fixture.ServiceAccountCredentialsTestHelper.encodeServiceCredentials
import static io.micronaut.gcp.credentials.fixture.ServiceAccountCredentialsTestHelper.generatePrivateKey

class SubscriberAuthenticationFailureSpec extends Specification {

    ServiceAccountTestListener listener

    PollingConditions conditions = new PollingConditions(timeout: 5)

    SimpleStreamsListener captured = new SimpleStreamsListener()

    @AutoCleanup("stop")
    StandardStreamsCapturer capturer = new StandardStreamsCapturer()

    void setup() {
        capturer.addStandardStreamsListener(captured)
        capturer.start()
    }

    void "subscription with invalid authentication credentials causes immediate subscriber termination"() {
        given:
        PrivateKey pk = generatePrivateKey()
        String encodedServiceAccountCredentials = encodeServiceCredentials(pk)
        EmbeddedServer gcp = ApplicationContext.run(EmbeddedServer, [
                "server.name" : "GoogleAuthServerTestFake",
                "micronaut.server.port" : 8080
        ])
        def ctx = ApplicationContext.run([
                "spec.name" : "AuthenticationFailureSpec",
                "gcp.projectId" : "micronaut-gcp-testing",
                (GoogleCredentialsConfiguration.PREFIX + ".encoded-key"): encodedServiceAccountCredentials
        ])

        when:
        def gc = ctx.getBean(GoogleCredentials)
        listener = ctx.getBean(ServiceAccountTestListener)
        def subscriberFactory = ctx.getBean(DefaultSubscriberFactory)

        then:
        gc != null
        listener != null
        conditions.eventually {
            !subscriberFactory.isRunning(ProjectSubscriptionName.of("micronaut-gcp-testing", "micronaut-gcp-topic1-sub"))
        }

        cleanup:
        ReflectionUtils.getFieldValue(GoogleCredentials.class, "defaultCredentialsProvider", gc)
                .ifPresent {
                    ReflectionUtils.setField(it.getClass(), "cachedCredentials", it, null)
                }
        ctx.close()
        gcp.close()
    }
}

@PubSubListener
@Requires(property = "spec.name", value = "AuthenticationFailureSpec")
class ServiceAccountTestListener {

    @Subscription("micronaut-gcp-topic1-sub")
    void receive(byte[] data) {

    }
}

@Requires(property = "server.name", value = "GoogleAuthServerTestFake")
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
