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
import spock.lang.Requires
import spock.lang.Specification

@Requires({
    env.GCP_SECRET_MANAGER_PROJECT_ID &&
        env.GCP_SECRET_MANAGER_IMPORT_PATH &&
        env.GOOGLE_APPLICATION_CREDENTIALS &&
        env.GCP_SECRET_MANAGER_EXPECTED_PROPERTY &&
        env.GCP_SECRET_MANAGER_EXPECTED_VALUE
})
class SecretManagerPropertySourceImporterIntegrationSpec extends Specification {

    void "imports secret manager config through micronaut config import against real gcp"() {
        given:
        String importUrl = "gcp-secret-manager://${System.getenv('GCP_SECRET_MANAGER_IMPORT_PATH')}?project-id=${System.getenv('GCP_SECRET_MANAGER_PROJECT_ID')}&credentials-location=${System.getenv('GOOGLE_APPLICATION_CREDENTIALS')}&format=properties"
        String expectedProperty = System.getenv('GCP_SECRET_MANAGER_EXPECTED_PROPERTY')
        String expectedValue = System.getenv('GCP_SECRET_MANAGER_EXPECTED_VALUE')
        ApplicationContext context = ApplicationContext.run([
                'spec.name'              : 'SecretManagerPropertySourceImporterIntegrationSpec',
                'micronaut.config.import': importUrl
        ], 'gcp')

        expect:
        context.getRequiredProperty(expectedProperty, String) == expectedValue

        cleanup:
        context.close()
    }
}
