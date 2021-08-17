package io.micronaut.gcp.function.http

import groovy.transform.InheritConstructors
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import spock.lang.Specification

class ErrorHandlingSpec extends Specification {


    void "test default bad request handler"() {
        when:"A page that is not there is visited"
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/errors/junk")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        then:"The local error handler is executed"
        googleResponse.statusCode == HttpStatus.BAD_REQUEST.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text.contains("\"_embedded\":{\"errors\":[{\"message\":\"Failed to convert argument [id]")
    }

    void "test custom local 404 handler"() {

        when:"A page that is not there is visited"
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/errors/10")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        then:"The local error handler is executed"
        googleResponse.statusCode == HttpStatus.NOT_FOUND.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == '{"description":"Locally not here!"}'
    }

    void "test custom global 404 handler"() {

        when:"A page that is not there is visited"
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/notThere/10")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        then:"The local error handler is executed"
        googleResponse.statusCode == HttpStatus.NOT_FOUND.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == '{"description":"Globally not here!"}'
    }

    void "test custom local exception handler"() {

        when:"A page that is not there is visited"
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/errors/local")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        then:"The local error handler is executed"
        googleResponse.statusCode == HttpStatus.BAD_REQUEST.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == '{"description":"Locally bad!: bad things happened"}'
    }

    void "test custom global exception handler"() {

        when:"A page that is not there is visited"
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/other/global")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        then:"The local error handler is executed"
        googleResponse.statusCode == HttpStatus.BAD_REQUEST.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == '{"description":"Globally bad!: bad things happened"}'
    }

    @Controller("/errors")
    static class ErrorsController {

        @Get("/{id}")
        HttpResponse byId(Long id) {
            return HttpResponse.notFound()
        }

        @Get("/local")
        HttpResponse local() {
            throw new MyException("bad things happened")
        }

        @Error(MyException)
        HttpResponse<MyError> localErrorHandler(MyException myException) {
            HttpResponse.<MyError>status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new MyError("Locally bad!: ${myException.message}"))
        }

        @Error(value = MyException, global = true)
        HttpResponse<MyError> globalErrorHandler(MyException myException) {
            HttpResponse.<MyError>status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new MyError("Globally bad!: ${myException.message}"))
        }

        @Error(status = HttpStatus.NOT_FOUND)
        HttpResponse<MyError> localNotFound() {
            HttpResponse.<MyError>status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new MyError("Locally not here!"))
        }

        @Error(status = HttpStatus.NOT_FOUND, global = true)
        HttpResponse<MyError> notFound() {
            HttpResponse.<MyError>status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new MyError("Globally not here!"))
        }
    }

    @Controller("/other")
    static class OtherController {
        @Get("/global")
        HttpResponse global() {
            throw new MyException("bad things happened")
        }
    }

    static class MyError {
        String description

        MyError(String description) {
            this.description = description
        }
    }

    @InheritConstructors
    static class MyException extends RuntimeException {

    }
}
