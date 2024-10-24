package io.micronaut.gcp.secretmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.secretmanager.client.SecretManagerClient
import reactor.core.publisher.Mono
import spock.lang.Specification

class LocationSecretManagerClientSpec extends Specification {

    void "missing regional secret"() {
        ApplicationContext context = ApplicationContext.run(["spec.name" : "LocationSecretManagerClientSpec", "gcp.projectId" : "first-gcp-project", "gcp.secret-manager.location" : "us-central1"])
        def client = context.getBean(SecretManagerClient)
        when:
            def result = Mono.from(client.getSecret("notFound", "latest", "first-gcp-project")).block()
        then:
            !result
    }

    void "fetch single regional secret"() {
        ApplicationContext context = ApplicationContext.run(["spec.name" : "LocationSecretManagerClientSpec", "gcp.projectId" : "first-gcp-project", "gcp.secret-manager.location" : "us-central1"])
        def client = context.getBean(SecretManagerClient)
        when:
            def result = Mono.from(client.getSecret("application", "latest", "first-gcp-project")).block()
        then:
            result.name == "application"
            result.contents
    }

}
