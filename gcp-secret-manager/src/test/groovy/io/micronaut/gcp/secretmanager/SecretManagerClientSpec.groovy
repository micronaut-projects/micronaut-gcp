package io.micronaut.gcp.secretmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.secretmanager.client.SecretManagerClient
import spock.lang.Specification



class SecretManagerClientSpec extends Specification {

    void "missing secret"() {
        ApplicationContext context = ApplicationContext.run(["spec.name" : "SecretManagerClientSpec", "gcp.projectId" : "first-gcp-project"])
        def client = context.getBean(SecretManagerClient)
        when:
            def result = client.getSecret("notFound", "latest", "first-gcp-project").blockingGet()
        then:
            result == null
    }

    void "fetch single secret"() {
        ApplicationContext context = ApplicationContext.run(["spec.name" : "SecretManagerClientSpec", "gcp.projectId" : "first-gcp-project"])
        def client = context.getBean(SecretManagerClient)
        when:
            def result = client.getSecret("application", "latest", "first-gcp-project").blockingGet()
        then:
            result.getName() == "application"
            result.getContents() != null
    }

}
