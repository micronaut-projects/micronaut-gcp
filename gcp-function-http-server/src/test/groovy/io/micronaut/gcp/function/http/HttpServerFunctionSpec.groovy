package io.micronaut.gcp.function.http

import io.micronaut.function.http.ServerlessExchange
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import spock.lang.Specification

class HttpServerFunctionSpec extends Specification {


    void "test simple text response"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def response = new GoogleFunctionHttpResponse<Object>(googleResponse)
        new HttpServerFunction()
                .service(new ServerlessExchange(
                        HttpRequest.GET("/simple/text"),
                        response
                ))

        expect:
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'good'
    }

}
