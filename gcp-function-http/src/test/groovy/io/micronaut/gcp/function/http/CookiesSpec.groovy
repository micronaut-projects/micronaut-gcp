package io.micronaut.gcp.function.http

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Specification

class CookiesSpec extends Specification {

    void "test binding and receive cookies"() {

        given:
        def googleResponse = new MockGoogleResponse()
        def googleRequest = new MockGoogleRequest(HttpMethod.GET, "/parameters/cookies")
        googleRequest.addHeader(HttpHeaders.COOKIE, "myCookie=someValue")
        new HttpFunction()
                .service(googleRequest, googleResponse)

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.text == 'someValue'
        googleResponse.headers[HttpHeaders.SET_COOKIE] == ['foo=bar; Domain=http://foo.com; HTTPOnly']
    }
}
