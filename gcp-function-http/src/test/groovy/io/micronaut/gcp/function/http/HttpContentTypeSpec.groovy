package io.micronaut.gcp.function.http


import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.PendingFeature
import spock.lang.Specification

@MicronautTest
@Property(name = 'spec.name', value = 'HttpContentTypeSpec')
class HttpContentTypeSpec extends Specification {

    @PendingFeature(reason = "failing with BAD_REQUEST")
    void 'POST form url encoded body binding to pojo works'() {
        given:
        def function = new HttpFunction()
        def googleResponse = initMockResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/form", 'message=World')

        when:
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
        function.service(googleRequest, googleResponse)

        then:
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.text == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    @PendingFeature(reason = "failing with BAD_REQUEST")
    void "POST form url encoded body binding to pojo works if you don't specify body annotation"() {
        given:
        def function = new HttpFunction()
        def googleResponse = initMockResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/form/without-body-annotation", 'message=World')

        when:
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
        function.service(googleRequest, googleResponse)

        then:
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.text == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    @PendingFeature(reason = "failing with BAD_REQUEST")
    void "POST form-url-encoded with Body annotation and a nested attribute"() {
        given:
        def function = new HttpFunction()
        def googleResponse = initMockResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/form/nested-attribute", 'message=World')

        when:
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
        function.service(googleRequest, googleResponse)

        then:
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.text == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    void "POST application-json with Body annotation and a nested attribute"() {
        given:
        def function = new HttpFunction()
        def googleResponse = initMockResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/form/json-nested-attribute", '{"message":"World"}')

        when:
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        function.service(googleRequest, googleResponse)

        then:
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.text == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    @PendingFeature(reason = "failing with BAD_REQUEST")
    // Cannot convert type [class io.micronaut.core.convert.value.ConvertibleValuesMap] to target type: class io.micronaut.gcp.function.http.MessageCreate
    void "POST application-json without Body annotation"() {
        given:
        def function = new HttpFunction()
        def googleResponse = initMockResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/form/json-without-body-annotation", '{"message":"World"}')

        when:
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        function.service(googleRequest, googleResponse)

        then:
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.text == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    void "POST application-json with Body annotation and a nested attribute and Map return rendered as JSON"() {
        given:
        def function = new HttpFunction()
        def googleResponse = initMockResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/form/json-nested-attribute-with-map-return", '{"message":"World"}')

        when:
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        function.service(googleRequest, googleResponse)

        then:
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.text == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    void "POST application-json with Body annotation and Object return rendered as JSON"() {
        given:
        def function = new HttpFunction()
        def googleResponse = initMockResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/form/json-with-body-annotation-and-with-object-return", '{"message":"World"}')

        when:
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        function.service(googleRequest, googleResponse)

        then:
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.text == '{"greeting":"Hello World"}'

        cleanup:
        function.close()
    }

    void "POST json with @Body annotation"() {
        given:
        def function = new HttpFunction()
        def googleResponse = initMockResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/form/json-with-body", '{"message":"World"}')

        when:
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        function.service(googleRequest, googleResponse)

        then:
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.text == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    private MockGoogleResponse initMockResponse() {
        MockGoogleResponse response = new MockGoogleResponse()
        response.setContentType(MediaType.APPLICATION_JSON)
        return response
    }
}
