package io.micronaut.gcp.secretmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@RestoreSystemProperties
class LocationSecretManagerConfigSpec extends Specification {

    void "load first project for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                      : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"     : "secret-manager-regional-test",
                    "micronaut.config-client.enabled": true,
                    "gcp.projectId"                  : "first-gcp-project",
                    "gcp.secret-manager.location"    : "us-central1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            "silver" == context.getRequiredProperty("acme.customer.tier", String)
            !context.getRequiredProperty("application.debug", Boolean)
            !context.containsProperty("sm.password")
            !context.containsProperty("custom.value")

        cleanup:
            context.stop()
    }

    void "load first project, explicitly with default config files for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                                : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"               : "secret-manager-regional-test",
                    "micronaut.config-client.enabled"          : true,
                    "gcp.projectId"                            : "first-gcp-project",
                    "gcp.secret-manager.default-config-enabled": true,
                    "gcp.secret-manager.location"              : "us-central1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            "silver" == context.getRequiredProperty("acme.customer.tier", String)
            !context.getRequiredProperty("application.debug", Boolean)
            !context.containsProperty("sm.password")
            !context.containsProperty("custom.value")

        cleanup:
            context.stop()
    }

    void "load first project, but without default config files for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                                : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"               : "secret-manager-regional-test",
                    "micronaut.config-client.enabled"          : true,
                    "gcp.projectId"                            : "first-gcp-project",
                    "gcp.secret-manager.default-config-enabled": false,
                    "gcp.secret-manager.location"              : "us-central1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            !context.containsProperty("acme.customer.tier")
            !context.containsProperty("application.debug")
            !context.containsProperty("sm.password")
            !context.containsProperty("custom.value")

        cleanup:
            context.stop()
    }

    void "load first project with custom config for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                           : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"          : "secret-manager-regional-test",
                    "micronaut.config-client.enabled"     : true,
                    "gcp.projectId"                       : "first-gcp-project",
                    "gcp.secret-manager.custom-configs[0]": "custom",
                    "gcp.secret-manager.location"         : "us-central1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            "silver" == context.getRequiredProperty("acme.customer.tier", String)
            !context.getRequiredProperty("application.debug", Boolean)
            !context.containsProperty("sm.password")
            "bar" == context.getRequiredProperty("custom.value", String)

        cleanup:
            context.stop()
    }

    void "load first project with custom config, but without default config files for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                                : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"               : "secret-manager-regional-test",
                    "micronaut.config-client.enabled"          : true,
                    "gcp.projectId"                            : "first-gcp-project",
                    "gcp.secret-manager.custom-configs[0]"     : "custom",
                    "gcp.secret-manager.default-config-enabled": false,
                    "gcp.secret-manager.location"              : "us-central1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            !context.containsProperty("acme.customer.tier")
            !context.containsProperty("application.debug")
            !context.containsProperty("sm.password")
            "bar" == context.getRequiredProperty("custom.value", String)

        cleanup:
            context.stop()
    }

    void "load first project with keys for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                      : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"     : "secret-manager-regional-test",
                    "micronaut.config-client.enabled": true,
                    "gcp.projectId"                  : "first-gcp-project",
                    "gcp.secret-manager.keys[0]"     : "password",
                    "gcp.secret-manager.location"    : "us-central1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            "silver" == context.getRequiredProperty("acme.customer.tier", String)
            !context.getRequiredProperty("application.debug", Boolean)
            "location-secret" == context.getRequiredProperty("sm.password", String)

        cleanup:
            context.stop()
    }

    void "load first project with keys, but without default config files for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                                : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"               : "secret-manager-regional-test",
                    "micronaut.config-client.enabled"          : true,
                    "gcp.projectId"                            : "first-gcp-project",
                    "gcp.secret-manager.keys[0]"               : "password",
                    "gcp.secret-manager.default-config-enabled": false,
                    "gcp.secret-manager.location"              : "us-central1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            !context.containsProperty("acme.customer.tier")
            !context.containsProperty("application.debug")
            "location-secret" == context.getRequiredProperty("sm.password", String)

        cleanup:
            context.stop()
    }

    void "load first project normalized with keys for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                      : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"     : "secret-manager-regional-test",
                    "micronaut.config-client.enabled": true,
                    "gcp.projectId"                  : "first-gcp-project",
                    "gcp.secret-manager.keys[0]"     : "DB_PASSWORD",
                    "gcp.secret-manager.keys[1]"     : "dbUser",
                    "gcp.secret-manager.location"    : "us-central1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            "silver" == context.getRequiredProperty("acme.customer.tier", String)
            !context.getRequiredProperty("application.debug", Boolean)
            "location-secret" == context.getRequiredProperty("sm.db.password", String)
            "location-user" == context.getRequiredProperty("sm.db.user", String)

        cleanup:
            context.stop()
    }

    void "load second project for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                      : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"     : "secret-manager-regional-test",
                    "micronaut.config-client.enabled": true,
                    "gcp.projectId"                  : "second-gcp-project",
                    "gcp.secret-manager.location"    : "us-central1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            context.getRequiredProperty("application.debug", Boolean)
            -1    == context.getRequiredProperty("application.server.port", Integer)

        cleanup:
            context.stop()
    }

    void "load first project other location for non-existing regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                      : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"     : "secret-manager-regional-test",
                    "micronaut.config-client.enabled": true,
                    "gcp.projectId"                  : "first-gcp-project",
                    "gcp.secret-manager.location"    : "us-east1",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            !context.containsProperty("acme.customer.tier")
            !context.containsProperty("application.debug")
            !context.containsProperty("sm.password")
            !context.containsProperty("custom.value")

        cleanup:
            context.stop()
    }

    void "load first project other location for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                      : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"     : "secret-manager-regional-test",
                    "micronaut.config-client.enabled": true,
                    "gcp.projectId"                  : "first-gcp-project",
                    "gcp.secret-manager.location"    : "us-east5",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            "bronze" == context.getRequiredProperty("acme.customer.tier", String)
            context.getRequiredProperty("application.debug", Boolean)
            !context.containsProperty("sm.password")
            !context.containsProperty("custom.value")

        cleanup:
            context.stop()
    }

    void "load first project other location with custom config for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                           : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"          : "secret-manager-regional-test",
                    "micronaut.config-client.enabled"     : true,
                    "gcp.projectId"                       : "first-gcp-project",
                    "gcp.secret-manager.custom-configs[0]": "custom",
                    "gcp.secret-manager.location"         : "us-east5",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            "bronze" == context.getRequiredProperty("acme.customer.tier", String)
            context.getRequiredProperty("application.debug", Boolean)
            !context.containsProperty("sm.password")
            "foobar" == context.getRequiredProperty("custom.value", String)

        cleanup:
            context.stop()
    }

    void "load first project other location with keys for regional secrets"() {

        given:
            Map<String, Object> properties = [
                    "spec.name"                      : "LocationSecretManagerConfigSpec",
                    "micronaut.application.name"     : "secret-manager-regional-test",
                    "micronaut.config-client.enabled": true,
                    "gcp.projectId"                  : "first-gcp-project",
                    "gcp.secret-manager.keys[0]"     : "password",
                    "gcp.secret-manager.location"    : "us-east5",
            ]
            System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
            ApplicationContext context = ApplicationContext.run(properties, "gcp")

        expect:
            "bronze" == context.getRequiredProperty("acme.customer.tier", String)
             context.getRequiredProperty("application.debug", Boolean)
            "other-location-secret" == context.getRequiredProperty("sm.password", String)

        cleanup:
            context.stop()
    }

}
