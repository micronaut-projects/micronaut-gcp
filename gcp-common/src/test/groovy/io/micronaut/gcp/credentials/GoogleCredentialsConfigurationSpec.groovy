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
package io.micronaut.gcp.credentials

import com.google.auth.oauth2.GoogleCredentials
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
@Requires(env = Environment.GOOGLE_COMPUTE)
class GoogleCredentialsConfigurationSpec extends Specification {

    @Inject GoogleCredentialsConfiguration configuration
    @Inject ApplicationContext context

    void "test google credentials configuration"() {
        expect:
        configuration.scopes == GoogleCredentialsConfiguration.DEFAULT_SCOPES
        context.getBean(GoogleCredentials)
    }

    void "test setLocation updates location"() {
        given:
        GoogleCredentialsConfiguration googleCredentialsConfiguration = new GoogleCredentialsConfiguration()

        when:
        googleCredentialsConfiguration.setLocation("test")

        then:
        googleCredentialsConfiguration.getLocation().isPresent()
        googleCredentialsConfiguration.getLocation().get() == "test"
    }
}
