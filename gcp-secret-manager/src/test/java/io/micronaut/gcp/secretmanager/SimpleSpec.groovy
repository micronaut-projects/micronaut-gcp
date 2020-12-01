package io.micronaut.gcp.secretmanager

import spock.lang.Specification

class SimpleSpec extends Specification{

    void "test"() {
        when:
            def sum = 1
        then:
            sum > 0
    }
}
