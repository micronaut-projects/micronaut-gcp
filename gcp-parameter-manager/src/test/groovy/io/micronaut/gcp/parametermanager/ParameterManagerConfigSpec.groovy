package io.micronaut.gcp.parametermanager

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.context.exceptions.ConfigurationException
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@RestoreSystemProperties
class ParameterManagerConfigSpec extends Specification {

    void "load first project"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"     : "parameter-manager-test",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.projectId"                  : "first-gcp-project"])
        expect:
        !context.containsProperties("custom.value")
        !context.containsProperties("acme.customer.tier")
        !context.containsProperties("logging.level")
        !context.containsProperties("logging.file.name")
        !context.containsProperties("logging.enabled")
        !context.containsProperties("pm.db.password")
        !context.containsProperties("pm.db.user")
        cleanup:
        context.stop()
    }

    void "load first project with custom config"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                              : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"             : "parameter-manager-test",
                                                             "micronaut.config-client.enabled"        : true,
                                                             "gcp.projectId"                          : "first-gcp-project",
                                                             "gcp.parameter-manager.custom-configs[0]": "microParam/v1"])
        expect:
        "foo" == context.getRequiredProperty("custom.value", String)
        "gold" == context.getRequiredProperty("acme.customer.tier", String)
        cleanup:
        context.stop()
    }

    void "load first project with multiple custom config"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                              : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"             : "parameter-manager-test",
                                                             "micronaut.config-client.enabled"        : true,
                                                             "gcp.projectId"                          : "first-gcp-project",
                                                             "gcp.parameter-manager.custom-configs[0]": "microParam/v1",
                                                             "gcp.parameter-manager.custom-configs[1]": "otherParam/v2"])
        expect:
        "foo" == context.getRequiredProperty("custom.value", String)
        "gold" == context.getRequiredProperty("acme.customer.tier", String)
        "INFO" == context.getRequiredProperty("logging.level", String)
        "logs/application.log" == context.getRequiredProperty("logging.file.name", String)
        true == context.getRequiredProperty("logging.enabled", Boolean)
        cleanup:
        context.stop()
    }

    void "load first project with keys"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"     : "parameter-manager-test",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.projectId"                  : "first-gcp-project",
                                                             "gcp.parameter-manager.keys[0]"  : "DB_PASSWORD/firstversion",
                                                             "gcp.parameter-manager.keys[1]"  : "dbUser/vers1"])
        expect:
        "very-sensitive-password" == context.getRequiredProperty("pm.db.password", String)
        "sqluser" == context.getRequiredProperty("pm.db.user", String)
        cleanup:
        context.stop()
    }

    void "load first project with both custom config and keys"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                              : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"             : "parameter-manager-test",
                                                             "micronaut.config-client.enabled"        : true,
                                                             "gcp.projectId"                          : "first-gcp-project",
                                                             "gcp.parameter-manager.custom-configs[0]": "microParam/v1",
                                                             "gcp.parameter-manager.custom-configs[1]": "otherParam/v2",
                                                             "gcp.parameter-manager.keys[0]"          : "DB_PASSWORD/firstversion",
                                                             "gcp.parameter-manager.keys[1]"          : "dbUser/vers1"])
        expect:
        "foo" == context.getRequiredProperty("custom.value", String)
        "gold" == context.getRequiredProperty("acme.customer.tier", String)
        "INFO" == context.getRequiredProperty("logging.level", String)
        "logs/application.log" == context.getRequiredProperty("logging.file.name", String)
        true == context.getRequiredProperty("logging.enabled", Boolean)
        "very-sensitive-password" == context.getRequiredProperty("pm.db.password", String)
        "sqluser" == context.getRequiredProperty("pm.db.user", String)
        cleanup:
        context.stop()
    }

    void "load second project"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        ApplicationContext context = ApplicationContext.run(["spec.name"                              : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"             : "parameter-manager-test",
                                                             "micronaut.config-client.enabled"        : true,
                                                             "gcp.projectId"                          : "second-gcp-project",
                                                             "gcp.parameter-manager.custom-configs[0]": "microSecondParam/v1",
                                                             "gcp.parameter-manager.keys[0]"          : "dbuser/vers1"])
        expect:
        "second-foo" == context.getRequiredProperty("custom.value", String)
        "golden" == context.getRequiredProperty("acme.customer.tier", String)
        "secondsqluser" == context.getRequiredProperty("pm.dbuser", String)
        cleanup:
        context.stop()
    }

    void "empty parameter reference"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        when:
        ApplicationContext context = ApplicationContext.run(["spec.name"                              : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"             : "parameter-manager-test",
                                                             "micronaut.config-client.enabled"        : true,
                                                             "gcp.projectId"                          : "first-gcp-project",
                                                             "gcp.parameter-manager.custom-configs[0]": ""])
        then:
        Exception ex = thrown(Exception)
        ex instanceof ConfigurationException
        "Parameter reference must not be empty" == ex.message
    }

    void "invalid parameter reference without slash"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        when:
        ApplicationContext context = ApplicationContext.run(["spec.name"                              : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"             : "parameter-manager-test",
                                                             "micronaut.config-client.enabled"        : true,
                                                             "gcp.projectId"                          : "first-gcp-project",
                                                             "gcp.parameter-manager.custom-configs[0]": "microParamv1"])
        then:
        Exception ex = thrown(Exception)
        ex instanceof ConfigurationException
        "Invalid parameter format. The expected format is '<parameter_name>/<parameter_version>', but the value was: microParamv1" == ex.message
    }

    void "invalid parameter reference without parameter name"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        when:
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"     : "parameter-manager-test",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.projectId"                  : "first-gcp-project",
                                                             "gcp.parameter-manager.keys[0]"  : "/v1"])
        then:
        Exception ex = thrown(Exception)
        ex instanceof ConfigurationException
        "Parameter name must not be empty: /v1" == ex.message
    }

    void "invalid parameter reference without parameter version"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        when:
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"     : "parameter-manager-test",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.projectId"                  : "first-gcp-project",
                                                             "gcp.parameter-manager.keys[0]"  : "microParam/"])
        then:
        Exception ex = thrown(Exception)
        ex instanceof ConfigurationException
        "Parameter version must not be empty: microParam/" == ex.message
    }

    void "invalid parameter reference with multiple slashes"() {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, "true")
        when:
        ApplicationContext context = ApplicationContext.run(["spec.name"                      : "ParameterManagerConfigSpec",
                                                             "micronaut.application.name"     : "parameter-manager-test",
                                                             "micronaut.config-client.enabled": true,
                                                             "gcp.projectId"                  : "first-gcp-project",
                                                             "gcp.parameter-manager.keys[0]"  : "microParam/some/v1"])
        then:
        Exception ex = thrown(Exception)
        ex instanceof ConfigurationException
        "Parameter name must not contain '/': microParam/some/v1" == ex.message
    }
}
