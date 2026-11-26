package io.micronaut.gcp.tracing.zipkin

import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.credentials.GoogleCredentialsConfiguration
import io.micronaut.gcp.credentials.fixture.ServiceAccountCredentialsTestHelper
import spock.lang.Issue
import spock.lang.Specification
import zipkin2.reporter.Sender

import java.security.PrivateKey

class StackdriverConfigurationSpec extends Specification {

    @Issue("https://github.com/micronaut-projects/micronaut-gcp/issues/761")
    void "native image metadata includes grpc auth jwt service account methods"() {
        when:
        URL metadata = getClass().classLoader.getResource(
                "META-INF/native-image/io.micronaut/gcp-tracing/reflect-config.json")
        String reflectConfig = metadata?.text

        then:
        reflectConfig
        reflectConfig.contains('"name": "com.google.auth.oauth2.ServiceAccountCredentials"')
        reflectConfig.contains('"allPublicMethods": true')
        reflectConfig.contains('"name": "getQuotaProjectId"')
        reflectConfig.contains('"name": "com.google.auth.oauth2.ServiceAccountJwtAccessCredentials"')
        reflectConfig.contains('"name": "com.google.auth.oauth2.ServiceAccountJwtAccessCredentials$Builder"')
        reflectConfig.contains('"name": "setQuotaProjectId"')
    }

    @Issue("https://github.com/micronaut-projects/micronaut-gcp/issues/1045")
    void "application starts successfully with stackdriver and zipkin enabled"() {
        given:
        PrivateKey pk = ServiceAccountCredentialsTestHelper.generatePrivateKey()
        File serviceAccountCredentials = ServiceAccountCredentialsTestHelper.writeServiceCredentialsToTempFile(pk)
        ApplicationContext ctx = ApplicationContext.run([
                "gcp.project-id" : "test-project",
                (GoogleCredentialsConfiguration.PREFIX + ".location"): serviceAccountCredentials.getPath(),
                "tracing.zipkin.enabled": true
        ])

        when:
        Sender sender = ctx.getBean(Sender.class)

        then:
        noExceptionThrown()
        sender
        
        cleanup:
        ctx.close()
    }
}
