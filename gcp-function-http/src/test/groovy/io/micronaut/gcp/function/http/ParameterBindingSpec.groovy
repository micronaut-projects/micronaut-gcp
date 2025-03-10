package io.micronaut.gcp.function.http

import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.PendingFeature
import spock.lang.Specification

class ParameterBindingSpec extends Specification {

    void "test URI parameters"() {

        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/uri/Foo")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == 'Hello Foo'
    }

    void "test invalid HTTP method"() {
        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/uri/Foo")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.METHOD_NOT_ALLOWED.code
        def allow = googleResponse.headers[HttpHeaders.ALLOW]
        allow == ["HEAD,GET"]
    }

    void "test query value"() {

        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/query")
        googleRequest.addParameter("q", "Foo")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == 'Hello Foo'
    }

    void "test all parameters"() {

        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/allParams")
        googleRequest.addParameter("name", "Foo")
        googleRequest.addParameter("age", "20")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == 'Hello Foo 20'
    }

    void "test header value"() {

        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/header")
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain;q=1.0")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == 'Hello text/plain;q=1.0'
    }

    void "test request and response"() {

        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/reqAndRes")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.ACCEPTED.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'Good'
    }

    void "test string body"() {

        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/stringBody", "Foo")
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == 'Hello Foo'
    }

    void "test writable"() {
        given:
        MockGoogleResponse googleResponse = executeWritableRequest()

        expect:
        googleResponse.statusCode == HttpStatus.CREATED.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'Hello Foo'
    }

    private static MockGoogleResponse executeWritableRequest() {
        HttpResponse googleResponse = new MockGoogleResponse()
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/writable", "Foo")
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
        new HttpFunction().service(googleRequest, googleResponse)
        googleResponse
    }

    @PendingFeature
    void "test writable header response"() {
        given:
        MockGoogleResponse googleResponse = executeWritableRequest()

        expect:
        googleResponse.headers["Foo"] == ['Bar']
    }

    void "test JSON POJO body"() {

        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        String json = '{"name":"bar","age":30}'
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/jsonBody", json)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == json
    }

    void "test JSON POJO body - invalid JSON"() {

        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        String json = '{"name":"bar","age":30'
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/jsonBody", json)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.BAD_REQUEST.code
        googleResponse.text.contains("Error decoding JSON stream for type")
    }


    void "test JSON POJO body with no @Body binds to arguments"() {

        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        String json = '{"name":"bar","age":20}'
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/jsonBodySpread", json)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == json
    }

    void "full Micronaut request and response"() {
        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        String json = '{"name":"bar","age":20}'
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/fullRequest", json)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == json
        googleResponse.headers["Foo"] == ['Bar']
    }


    void "full Micronaut request and response - invalid JSON"() {
        given:
        HttpResponse googleResponse = new MockGoogleResponse()
        String json = '{"name":"bar","age":20'
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/fullRequest", json)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.BAD_REQUEST.code
        googleResponse.text.contains("Error decoding request body")
    }

    void "test multipart binding"() {
        given:
        HttpRequest googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/multipart")
        googleRequest.addParameter("foo", "bar")
        googleRequest.parts.put("one", new MockGoogleHttpPart("one.json", '{"name":"bar","age":20}', "application/json"))
        googleRequest.parts.put("two", new MockGoogleHttpPart("two.txt", 'Whatever', "text/plain"))
        googleRequest.parts.put("three", new MockGoogleHttpPart("some.doc", 'My Doc', "application/octet-stream"))
        googleRequest.parts.put("four", new MockGoogleHttpPart("raw.doc", 'Another Doc', "application/octet-stream"))
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
        HttpResponse googleResponse = new MockGoogleResponse()
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.text == 'Good: true'

    }
}
