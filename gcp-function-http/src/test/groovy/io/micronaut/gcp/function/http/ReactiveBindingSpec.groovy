package io.micronaut.gcp.function.http

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.json.JsonMapper
import spock.lang.Specification

class ReactiveBindingSpec extends Specification {

    void "test json array as flowable"() {
        given:
        def function = new HttpFunction()
        JsonMapper mapper = function.applicationContext.getBean(JsonMapper)
        def json = [
                new Person("Fred"),
                new Person("Bob")
        ]
        def string = mapper.writeValueAsString(json)
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
