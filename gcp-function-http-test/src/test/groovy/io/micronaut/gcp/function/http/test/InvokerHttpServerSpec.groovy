package io.micronaut.gcp.function.http.test

import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Part
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.multipart.MultipartBody
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
class InvokerHttpServerSpec extends Specification {

    @Inject
    @Client('/')
    HttpClient client

    void 'test invoke function via server'() {
        when:
        def result = client.toBlocking().retrieve('/test')

        then:
        result == 'good'
    }

    void 'test invoke post via server'() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/test', "body")
                .contentType(MediaType.TEXT_PLAIN), String)

        then:
        result == 'goodbody'
    }

    void 'test invoke multipart post via server'() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/test/multipart', MultipartBody.builder()
                .addPart('name', 'Fred')
                .addPart('file', 'file.txt', MediaType.TEXT_PLAIN_TYPE, 'Some text'.bytes)
                .build())
                .contentType(MediaType.MULTIPART_FORM_DATA_TYPE), String)

        then:
        result == 'Fred: Some text'
    }


    @Controller('/test')
    static class TestController {
        @Get(value = '/', produces = MediaType.TEXT_PLAIN)
        String test() {
            return 'good'
        }

        @Post(value = '/', processes = MediaType.TEXT_PLAIN)
        String test(@Body String body) {
            return 'good' + body
        }

        @Post(value = '/multipart', consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
        String multipart(String name, @Part('file') String file) {
            return name + ': ' + file
        }
    }
}
