package io.micronaut.gcp.secretmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.secretmanager.client.SecretManagerClient
import reactor.core.publisher.Mono
import spock.lang.Specification



class SecretManagerClientSpec extends Specification {

    void "missing secret"() {
        ApplicationContext context = ApplicationContext.run(["spec.name" : "SecretManagerClientSpec", "gcp.projectId" : "first-gcp-project"])
        def client = context.getBean(SecretManagerClient)
        when:
            def result = Mono.from(client.getSecret("notFound", "latest", "first-gcp-project")).block()
        then:
            result == null
    }

    void "fetch single secret"() {
        ApplicationContext context = ApplicationContext.run(["spec.name" : "SecretManagerClientSpec", "gcp.projectId" : "first-gcp-project"])
        def client = context.getBean(SecretManagerClient)
        when:
            def result = Mono.from(client.getSecret("application", "latest", "first-gcp-project")).block()
        then:
            result.getName() == "application"
            result.getContents() != null
    }

}
