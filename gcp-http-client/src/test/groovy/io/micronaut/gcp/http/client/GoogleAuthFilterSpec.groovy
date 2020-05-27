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
package io.micronaut.gcp.http.client

import io.micronaut.context.annotation.Property
import io.micronaut.context.env.Environment
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(environments = Environment.GOOGLE_COMPUTE)
@Property(name = "gcp.http.client.auth.patterns", value = "/test/**")
class GoogleAuthFilterSpec extends Specification {

    @Inject
    TestClient client

    void 'test apply auth to requests'() {
        when:
        def result = client.home()

        then:
        result == 'good'

        when:
        client.applyToMe()

        then:
        def e = thrown(HttpClientException)
        e.message.contains('metadata: nodename nor servname provided') || e.message.contains('metadata: Temporary failure in name resolution') || e.message.contains('metadata: Name or service not known')
    }


    @Client("/")
    static interface TestClient {
        @Get("/")
        String home()

        @Get("/test/foo")
        String applyToMe()
    }

    @Controller("/")
    static class TestController {
        @Get("/")
        String home() {
            return "good"
        }

        @Get("/test/foo")
        String applyToMe() {
            return "ok"
        }
    }
}
