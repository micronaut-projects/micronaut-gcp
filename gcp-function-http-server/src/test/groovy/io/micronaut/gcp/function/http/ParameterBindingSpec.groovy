package io.micronaut.gcp.function.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Specification

class ParameterBindingSpec extends Specification {

    void "test URI parameters"() {

        given:
        def googleResponse = new MockGoogleResponse()
        new HttpServerFunction()
                .service(HttpRequest.GET("/parameters/uri/Foo"), googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'Hello Foo'
    }
}
