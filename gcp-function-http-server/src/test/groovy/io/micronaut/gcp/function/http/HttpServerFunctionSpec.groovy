package io.micronaut.gcp.function.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import spock.lang.Specification

class HttpServerFunctionSpec extends Specification {


    void "test simple text response"() {

        given:
        def googleResponse = new MockGoogleResponse()
        new HttpServerFunction()
                .service(HttpRequest.GET("/simple/text"), googleResponse)

        expect:
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'good'
    }

    void "test simple JSON POJO response"() {

        given:
        def googleResponse = new MockGoogleResponse()
        new HttpServerFunction()
                .service(HttpRequest.GET("/simple/simplePojo"), googleResponse)

        expect:
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == '{"name":"good"}'
    }

}
