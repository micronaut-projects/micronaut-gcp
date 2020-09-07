package io.micronaut.gcp.function.http.test

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class InvokerHttpServerSpec extends Specification {

    @Inject
    @Client('/')
    RxHttpClient client

    void 'test invoke function via server'() {
        when:
        def result = client.retrieve('/test').blockingFirst()

        then:
        result == 'good'
    }


    @Controller('/test')
    static class TestController {
        @Get(value = '/', produces = MediaType.TEXT_PLAIN)
        String test() {
            return 'good'
        }
    }
}
