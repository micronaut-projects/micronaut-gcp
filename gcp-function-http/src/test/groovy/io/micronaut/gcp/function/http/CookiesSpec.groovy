package io.micronaut.gcp.function.http

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.cookie.Cookie
import spock.lang.Specification

class CookiesSpec extends Specification {

    void "test binding and receive cookies"() {
        given:
        GoogleHttpResponse googleResponse = new HttpFunction()
                .invoke(HttpRequest.GET("/parameters/cookies").cookie(
                        Cookie.of("myCookie", "someValue")
                ))

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.APPLICATION_JSON
        googleResponse.bodyAsText == 'someValue'
        googleResponse.headers[HttpHeaders.SET_COOKIE] == ['foo=bar; Domain=https://foo.com; HTTPOnly']
    }
}
