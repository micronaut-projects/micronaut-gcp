package io.micronaut.gcp.secretmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@RestoreSystemProperties
class SecretManagerConfigSpec extends Specification {

    void "load first project"() {

        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "SecretManagerConfigSpec",
                                                             "micronaut.application.name"     : "secret-manager-test",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.projectId"                  : "first-gcp-project"], "gcp")

        expect:
        "gold" == context.getRequiredProperty("acme.customer.tier", String)
        true == context.getRequiredProperty("application.debug", Boolean)
        false == context.containsProperty("sm.password")
        cleanup:
        context.stop()
    }

    void "load first project with keys"() {

        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "SecretManagerConfigSpec",
                                                             "micronaut.application.name"     : "secret-manager-test",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.secret-manager.keys[0]"     : "password",
                                                             "gcp.projectId"                  : "first-gcp-project"], "gcp")

        expect:
        "gold" == context.getRequiredProperty("acme.customer.tier", String)
        true == context.getRequiredProperty("application.debug", Boolean)
        "secret" == context.getRequiredProperty("sm.password", String)
        cleanup:
        context.stop()
    }

    void "load second project"() {

        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "SecretManagerConfigSpec",
                                                             "micronaut.application.name"     : "secret-manager-test",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.projectId"                  : "second-gcp-project"], "gcp")
        expect:

        false == context.getRequiredProperty("application.debug", Boolean)
        cleanup:
        context.stop()
    }


}
