package io.micronaut.gcp.credentials

import com.google.auth.oauth2.GoogleCredentials
import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.NoSuchBeanException
import spock.lang.Specification

class GoogleCredentialsFactorySpec extends Specification {

    void "it can disable GoogleCredentials bean"() {
        given:
        def ctx = ApplicationContext.run([
                (GoogleCredentialsConfiguration.PREFIX + ".enabled"): false
        ])

        when:
        ctx.getBean(GoogleCredentials)

        then:
        thrown(NoSuchBeanException)
    }
}
