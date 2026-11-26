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
package io.micronaut.gcp.parametermanager

import io.micronaut.context.exceptions.ConfigurationException
import spock.lang.Specification

class ParameterNameParsingSpec extends Specification {

    void "a name without a version resolves to latest"() {
        when:
        def parsed = ParameterManagerConfigurationClient.parseNameAndVersion(raw)

        then:
        parsed.name() == name
        parsed.version() == version

        where:
        raw                        | name          | version
        'DB_PASSWORD'              | 'DB_PASSWORD' | 'latest'
        ' dbUser '                 | 'dbUser'      | 'latest'
        'DB_PASSWORD/firstversion' | 'DB_PASSWORD' | 'firstversion'
    }

    void "a trailing slash without a version is rejected"() {
        when:
        ParameterManagerConfigurationClient.parseNameAndVersion('DB_PASSWORD/')

        then:
        ConfigurationException ex = thrown()
        ex.message == 'Parameter version must not be empty: DB_PASSWORD/'
    }

    void "a name containing a slash is rejected"() {
        when:
        ParameterManagerConfigurationClient.parseNameAndVersion('my/param/2')

        then:
        ConfigurationException ex = thrown()
        ex.message == "Parameter name must not contain '/': my/param/2"
    }

    void "a missing name is rejected"() {
        when:
        ParameterManagerConfigurationClient.parseNameAndVersion('/v1')

        then:
        ConfigurationException ex = thrown()
        ex.message == 'Parameter name must not be empty: /v1'
    }

    void "a blank reference is rejected"() {
        when:
        ParameterManagerConfigurationClient.parseNameAndVersion('   ')

        then:
        ConfigurationException ex = thrown()
        ex.message == 'Parameter reference must not be empty'
    }
}
