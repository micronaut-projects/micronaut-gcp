package io.micronaut.gcp.function.http

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.serde.ObjectMapper
import spock.lang.Specification

class ReactiveBindingSpec extends Specification {

    void "test json array as flowable"() {
        given:
        def function = new HttpFunction()
        ObjectMapper mapper = function.applicationContext.getBean(ObjectMapper)
        def json = [
                new Person("Fred"),
                new Person("Bob")
        ]
        def string = new String(mapper.writeValueAsBytes(json))
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/reactive/jsonArray", string)
        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        function.service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.text == string
    }
}
