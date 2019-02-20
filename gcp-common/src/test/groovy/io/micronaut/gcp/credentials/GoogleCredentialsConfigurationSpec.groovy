package io.micronaut.gcp.credentials

import com.google.api.gax.core.GoogleCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class GoogleCredentialsConfigurationSpec extends Specification {

    @Inject GoogleCredentialsConfiguration configuration
    @Inject ApplicationContext context

    void "test google credentials configuration"() {
        expect:
        configuration.oauthScopes == GoogleCredentialsConfiguration.DEFAULT_SCOPES
        context.getBean(GoogleCredentialsProvider)
    }
}
