/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.http.server.tck.gcp.function.tests;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.tck.AssertionUtils;
import io.micronaut.http.tck.HttpResponseAssertion;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.micronaut.http.tck.TestScenario.asserts;


@SuppressWarnings({
    "java:S5960", // We're allowed assertions, as these are used in tests only
    "checkstyle:MissingJavadocType",
    "checkstyle:DesignForExtension"
})
public class StatusExecutesOnTest {
    public static final String SPEC_NAME = "StatusExecutesOnTest";

    /**
     * This tests reproduces verifies the fix introduced via https://github.com/micronaut-projects/micronaut-servlet/pull/438
     * @throws IOException IO Exception
     */
    @Test
    void testControllerReturningHttpStatus() throws IOException {
        asserts(SPEC_NAME,
            HttpRequest.GET("/http-response-status-executes-on"),
            (server, request) -> AssertionUtils.assertDoesNotThrow(server, request, HttpResponseAssertion.builder()
                .status(HttpStatus.CREATED)
                .build()));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @Controller("/http-response-status-executes-on")
    @ExecuteOn(TaskExecutors.IO)
    static class HttpResponseStatusController {

        @Get
        HttpResponse<?> index() throws InterruptedException {
            Thread.sleep(1000); // Necessary to trigger the race condition
            return HttpResponse.status(HttpStatus.CREATED);
        }
    }
}
