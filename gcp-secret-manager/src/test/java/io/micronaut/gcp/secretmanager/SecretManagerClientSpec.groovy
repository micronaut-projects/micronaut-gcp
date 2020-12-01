package io.micronaut.gcp.secretmanager

import io.micronaut.context.annotation.Property
import io.micronaut.gcp.secretmanager.client.SecretManagerClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest
@Property(name = "spec.name", value = "SecretManagerClientSpec")
@Property(name = "gcp.projectId", value = "first-gcp-project")
class SecretManagerClientSpec extends AbstractSecretManagerSpec {



    void "get single secret"() {
        when:
            def result = ""
        then:
            result != null
    }

}
