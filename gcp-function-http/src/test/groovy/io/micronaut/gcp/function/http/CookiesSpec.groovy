/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.function.http

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.cookie.Cookie
import spock.lang.Specification

class CookiesSpec extends Specification {

    void "test binding and receive cookies"() {

        given:
        def googleResponse = new HttpFunction()
                .invoke(HttpRequest.GET("/parameters/cookies").cookie(
                        Cookie.of("myCookie", "someValue")
                ))

        expect:
        googleResponse.statusCode == HttpStatus.OK.code
        googleResponse.contentType.get() == MediaType.TEXT_PLAIN
        googleResponse.bodyAsText == 'someValue'
        googleResponse.headers[HttpHeaders.SET_COOKIE] == ['foo=bar; Domain=http://foo.com; HTTPOnly']
    }
}
