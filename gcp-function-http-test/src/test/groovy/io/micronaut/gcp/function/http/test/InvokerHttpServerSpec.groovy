package io.micronaut.gcp.function.http.test

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.annotation.NonNull
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.PendingFeature
import spock.lang.Specification

import javax.validation.constraints.NotBlank

@MicronautTest
@Property(name = 'spec.name', value = 'InvokerHttpServerSpec')
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

    void 'POST form url encoded body binding to pojo works'() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/form', "message=World")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED), String)

        then:
        result == '{"message":"Hello World"}'
    }

    @PendingFeature(reason = "fails with HttpClientResponseException: Bad Request")
    void "POST form url encoded body binding to pojo works if you don't specify body annotation"() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/form/without-body-annotation', "message=World")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED), String)

        then:
        result == '{"message":"Hello World"}'
    }

    void "form-url-encoded with Body annotation and a nested attribute"() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/form/nested-attribute', "message=World")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED), String)

        then:
        result == '{"message":"Hello World"}'
    }

    void "application-json with Body annotation and a nested attribute"() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/form/json-nested-attribute', "{\"message\":\"World\"}")
                .contentType(MediaType.APPLICATION_JSON), String)

        then:
        result == '{"message":"Hello World"}'
    }

    @PendingFeature(reason = "fails with HttpClientResponseException: Bad Request")
    void "application-json without Body annotation"() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/form/json-without-body-annotation', "{\"message\":\"World\"}")
                .contentType(MediaType.APPLICATION_JSON), String)

        then:
        result == '{"message":"Hello World"}'
    }

    void "application-json with Body annotation and a nested attribute and Map return rendered as JSON"() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/form/json-nested-attribute-with-map-return', "{\"message\":\"World\"}")
                .contentType(MediaType.APPLICATION_JSON), String)

        then:
        result == '{"message":"Hello World"}'
    }

    void "application-json with Body annotation and Object return rendered as JSON"() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/form/json-with-body-annotation-and-with-object-return', "{\"message\":\"World\"}")
                .contentType(MediaType.APPLICATION_JSON), String)

        then:
        result == '{"greeting":"Hello World"}'
    }

    @Controller('/test')
    @Requires(property = 'spec.name', value = 'InvokerHttpServerSpec')
    static class TestController {
        @Get(value = '/', produces = MediaType.TEXT_PLAIN)
        String test() {
            return 'good'
        }

        @Post(value = '/', processes = MediaType.TEXT_PLAIN)
        String test(@Body String body) {
            return 'good' + body
        }
    }

    @Controller("/form")
    @Requires(property = 'spec.name', value = 'InvokerHttpServerSpec')
    static class FormController {

        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Post("/without-body-annotation")
        String withoutBodyAnnotation(MessageCreate messageCreate) {
            "{\"message\":\"Hello ${messageCreate.getMessage()}\"}";
        }

        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Post
        String save(@Body MessageCreate messageCreate) {
            "{\"message\":\"Hello ${messageCreate.getMessage()}\"}";
        }

        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Post("/nested-attribute")
        String save(@Body("message") String value) {
            "{\"message\":\"Hello ${value}\"}";
        }

        @Consumes(MediaType.APPLICATION_JSON)
        @Post("/json-without-body-annotation")
        String jsonWithoutBody(MessageCreate messageCreate) {
            "{\"message\":\"Hello ${messageCreate.message}\"}";
        }

        @Consumes(MediaType.APPLICATION_JSON)
        @Post("/json-nested-attribute")
        String jsonNestedAttribute(@Body("message") String value) {
            "{\"message\":\"Hello ${value}\"}";
        }

        @Consumes(MediaType.APPLICATION_JSON)
        @Post("/json-nested-attribute-with-map-return")
        Map<String, String> jsonNestedAttributeWithMapReturn(@Body("message") String value) {
            [message: "Hello ${value}".toString()]
        }

        @Consumes(MediaType.APPLICATION_JSON)
        @Post("/json-with-body-annotation-and-with-object-return")
        MyResponse jsonNestedAttributeWithObjectReturn(@Body MessageCreate messageCreate) {
            new MyResponse("Hello ${messageCreate.message}")
        }
    }

    @Introspected
    static class MessageCreate {

        @NonNull
        @NotBlank
        private final String message;

        MessageCreate(@NonNull String message) {
            this.message = message;
        }

        @NonNull
        String getMessage() {
            return message;
        }
    }

    @Introspected
    static class MyResponse {

        @NonNull
        @NotBlank
        private final String greeting;

        MyResponse(@NonNull String greeting) {
            this.greeting = greeting
        }

        @NonNull
        String getGreeting() {
            return greeting
        }
    }
}
