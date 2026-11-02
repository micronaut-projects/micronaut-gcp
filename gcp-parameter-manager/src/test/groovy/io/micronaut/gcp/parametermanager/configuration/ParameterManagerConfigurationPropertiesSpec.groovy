/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.gcp.parametermanager.configuration

import io.micronaut.context.ApplicationContext
import jakarta.validation.constraints.Pattern
import spock.lang.Specification

class ParameterManagerConfigurationPropertiesSpec extends Specification {

    void "accepts supported multi-region and regional parameter manager locations"() {
        given:
        ApplicationContext context = ApplicationContext.run(['gcp.parameter-manager.location': location])

        expect:
        context.getBean(ParameterManagerConfigurationProperties).location == location

        cleanup:
        context.close()

        where:
        location << ['us', 'eu', 'us-east1']
    }

    void "location is not restricted to a hardcoded list of regions"() {
        expect:
        ParameterManagerConfigurationProperties.getDeclaredField('location').getAnnotation(Pattern) == null
    }
}
