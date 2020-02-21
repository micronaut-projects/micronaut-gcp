package io.micronaut.gcp.function.http

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Specification

class ParameterBindingSpec extends Specification {

    void "test URI parameters"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/uri/Foo")
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'Hello Foo'
    }

    void "test query value"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/query")
        googleRequest.addParameter("q", "Foo")
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'Hello Foo'
    }

    void "test all parameters"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/allParams")
        googleRequest.addParameter("name", "Foo")
        googleRequest.addParameter("age", "20")
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'Hello Foo 20'
    }

    void "test header value"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/header")
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain;q=1.0")
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'Hello text/plain;q=1.0'
    }

    void "test request and response"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/reqAndRes")
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.ACCEPTED.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'Good'
    }

    void "test string body"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/stringBody", "Foo")
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'Hello Foo'
    }


    void "test JSON POJO body"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def json = '{"name":"bar","age":30}'
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/jsonBody", json)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == json
    }


    void "test JSON POJO body with no @Body binds to arguments"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def json = '{"name":"bar","age":20}'
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/jsonBodySpread", json)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == json
    }

    void "full Micronaut request and response"() {
        given:
        def googleResponse = new MockGoogleResponse()
        def json = '{"name":"bar","age":20}'
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/fullRequest", json)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == json
        googleResponse.headers["Foo"] == ['Bar']
    }


    void "full Micronaut request and response - invalid JSON"() {
        given:
        def googleResponse = new MockGoogleResponse()
        def json = '{"name":"bar","age":20'
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/fullRequest", json)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        new HttpServerFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.BAD_REQUEST.code
        googleResponse.message.contains("Error decoding request body")
    }
}
