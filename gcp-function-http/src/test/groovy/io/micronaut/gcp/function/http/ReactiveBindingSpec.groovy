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

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
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
