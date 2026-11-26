package io.micronaut.gcp.parametermanager

import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.parametermanager.client.ParameterManagerAccessClient
import reactor.core.publisher.Mono
import spock.lang.Specification

class LocationParameterManagerAccessClientSpec extends Specification {

    void "get method for missing regional parameter"() {
        ApplicationContext context = ApplicationContext.run(["spec.name": "ParameterManagerAccessClientSpec", "gcp.projectId": "first-gcp-project", "gcp.parameter-manager.location": "us-east1"])
        def client = context.getBean(ParameterManagerAccessClient)
        when:
            def result = Mono.from(client.getParameter("nonExistent", "v1")).block()
        then:
            !result
    }

    void "render method for missing regional parameter"() {
        ApplicationContext context = ApplicationContext.run(["spec.name": "ParameterManagerAccessClientSpec", "gcp.projectId": "first-gcp-project", "gcp.parameter-manager.location": "us-east1"])
        def client = context.getBean(ParameterManagerAccessClient)
        when:
            def result = Mono.from(client.getRenderedParameter("nonExistent", "v1", "first-gcp-project")).block()
        then:
            !result
    }

    void "fetch single regional parameter"() {
        ApplicationContext context = ApplicationContext.run(["spec.name": "ParameterManagerAccessClientSpec", "gcp.projectId": "first-gcp-project", "gcp.parameter-manager.location": "us-east1"])
        def client = context.getBean(ParameterManagerAccessClient)
        when:
            def result = Mono.from(client.getParameter("microParam", "v1", "first-gcp-project")).block()
        then:
            result.getName() == "microParam"
            result.getContents() != null
    }

    void "render single regional parameter"() {
        ApplicationContext context = ApplicationContext.run(["spec.name": "ParameterManagerAccessClientSpec", "gcp.projectId": "first-gcp-project", "gcp.parameter-manager.location": "us-east1"])
        def client = context.getBean(ParameterManagerAccessClient)
        when:
            def result = Mono.from(client.getRenderedParameter("microParam", "v1", "first-gcp-project")).block()
        then:
            result.getName() == "microParam"
            result.getContents() != null
    }
}
