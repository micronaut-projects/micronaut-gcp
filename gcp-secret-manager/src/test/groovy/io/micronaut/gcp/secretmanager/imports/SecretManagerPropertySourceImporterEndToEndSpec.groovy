/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.gcp.secretmanager.imports

import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.credentials.fixture.ServiceAccountCredentialsTestHelper
import io.micronaut.gcp.utils.LocalSecretManagerServiceClientFactory
import spock.lang.Specification

class SecretManagerPropertySourceImporterEndToEndSpec extends Specification {

    void "imports secret manager config through micronaut config import"() {
        given:
        String encodedKey = ServiceAccountCredentialsTestHelper.encodeServiceCredentials(ServiceAccountCredentialsTestHelper.generatePrivateKey())
        ApplicationContext context = ApplicationContext.run([
                'spec.name'                           : 'SecretManagerPropertySourceImporterEndToEndSpec',
                'micronaut.config.import.provider'   : 'gcp-secret-manager',
                'micronaut.config.import.path'       : 'application',
                'micronaut.config.import.project-id' : 'first-gcp-project',
                'micronaut.config.import.encoded-key': encodedKey
        ], 'gcp')

        expect:
        context.getRequiredProperty('application.debug', Boolean)
        !context.containsProperty('acme.customer.tier')

        cleanup:
        context.close()
    }
}
