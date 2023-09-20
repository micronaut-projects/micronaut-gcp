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
            false == context.containsProperty("custom.value")
        cleanup:
            context.stop()
    }

    void "load first project, explicitly with default config files"() {

        given:
        Map<String, Object> properties = [
                "spec.name"                                : "SecretManagerConfigSpec",
                "micronaut.application.name"               : "secret-manager-test",
                "micronaut.config-client.enabled"          : true,
                "gcp.projectId"                            : "first-gcp-project",
                "gcp.secret-manager.default-config-enabled": true,
        ]
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
        "gold" == context.getRequiredProperty("acme.customer.tier", String)
        true == context.getRequiredProperty("application.debug", Boolean)
        false == context.containsProperty("sm.password")
        false == context.containsProperty("custom.value")

        cleanup:
        context.stop()
    }

    void "load first project, but without default config files"() {

        given:
        Map<String, Object> properties = [
                "spec.name"                                : "SecretManagerConfigSpec",
                "micronaut.application.name"               : "secret-manager-test",
                "micronaut.config-client.enabled"          : true,
                "gcp.projectId"                            : "first-gcp-project",
                "gcp.secret-manager.default-config-enabled": false,
        ]
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
        false == context.containsProperty("acme.customer.tier")
        false == context.containsProperty("application.debug")
        false == context.containsProperty("sm.password")
        false == context.containsProperty("custom.value")

        cleanup:
        context.stop()
    }

    void "load first project with custom config"() {

        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "SecretManagerConfigSpec",
                                                             "micronaut.application.name"     : "secret-manager-test",
                                                             "gcp.secret-manager.custom-configs[0]"     : "custom",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.projectId"                  : "first-gcp-project"], "gcp")

        expect:
            "gold" == context.getRequiredProperty("acme.customer.tier", String)
            true == context.getRequiredProperty("application.debug", Boolean)
            false == context.containsProperty("sm.password")
            "foo" == context.getRequiredProperty("custom.value", String)
        cleanup:
        context.stop()
    }

    void "load first project with custom config, but without default config files"() {

        given:
        Map<String, Object> properties = [
                "spec.name"                                : "SecretManagerConfigSpec",
                "micronaut.application.name"               : "secret-manager-test",
                "micronaut.config-client.enabled"          : true,
                "gcp.projectId"                            : "first-gcp-project",
                "gcp.secret-manager.custom-configs[0]"     : "custom",
                "gcp.secret-manager.default-config-enabled": false,
        ]
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            false == context.containsProperty("acme.customer.tier")
            false == context.containsProperty("application.debug")
            false == context.containsProperty("sm.password")
            "foo" == context.getRequiredProperty("custom.value", String)

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

    void "load first project with keys, but without default config files"() {

        given:
        Map<String, Object> properties = [
                "spec.name"                                : "SecretManagerConfigSpec",
                "micronaut.application.name"               : "secret-manager-test",
                "micronaut.config-client.enabled"          : true,
                "gcp.projectId"                            : "first-gcp-project",
                "gcp.secret-manager.default-config-enabled": false,
                "gcp.secret-manager.keys[0]"               : "password",
        ]
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
        false == context.containsProperty("acme.customer.tier")
        false == context.containsProperty("application.debug")
        "secret" == context.getRequiredProperty("sm.password", String)

        cleanup:
        context.stop()
    }

    void "load first project normalized with keys"() {

        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "SecretManagerConfigSpec",
                                                             "micronaut.application.name"     : "secret-manager-test",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.secret-manager.keys[0]"     : "DB_PASSWORD",
                                                             "gcp.secret-manager.keys[1]"     : "dbUser",
                                                             "gcp.projectId"                  : "first-gcp-project"], "gcp")

        expect:
        "gold" == context.getRequiredProperty("acme.customer.tier", String)
        true == context.getRequiredProperty("application.debug", Boolean)
        "secret" == context.getRequiredProperty("sm.db.password", String)
        "user" == context.getRequiredProperty("sm.db.user", String)
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
        -1    == context.getRequiredProperty("application.server.port", Integer)
        cleanup:
        context.stop()
    }


}
