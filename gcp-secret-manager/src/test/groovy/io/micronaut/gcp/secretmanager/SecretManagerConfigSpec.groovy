package io.micronaut.gcp.secretmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@RestoreSystemProperties
class SecretManagerConfigSpec extends Specification{

    void "load defaults"() {
        given:
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(["spec.name": "SecretManagerConfigSpec",
                                                                 "micronaut.application.name": "secret-manager-test",
                                                                 "micronaut.config-client.enabled": true,
                                                                 "gcp.projectId": "first-gcp-project"], "gcp")

        expect:
            context.getBean(SecretManagerConfigurationClient)
            "gold" == context.getRequiredProperty("acme.customer.tier", String)
            true == context.getRequiredProperty("application.debug", Boolean)
        cleanup:
            context.stop()
    }

}
