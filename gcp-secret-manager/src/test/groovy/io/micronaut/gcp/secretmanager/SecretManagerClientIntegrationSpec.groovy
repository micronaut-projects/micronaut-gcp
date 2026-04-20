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
package io.micronaut.gcp.secretmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.secretmanager.client.SecretManagerClient
import reactor.core.publisher.Mono
import spock.lang.Requires
import spock.lang.Specification

@Requires({
    env.GCP_SECRET_MANAGER_PROJECT_ID &&
        env.GCP_SECRET_MANAGER_IMPORT_PATH &&
        env.GOOGLE_APPLICATION_CREDENTIALS
})
class SecretManagerClientIntegrationSpec extends Specification {

    void "fetches an existing secret against real gcp"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'spec.name'                     : 'SecretManagerClientIntegrationSpec',
                'gcp.projectId'                : System.getenv('GCP_SECRET_MANAGER_PROJECT_ID'),
                'gcp.credentials.location'     : System.getenv('GOOGLE_APPLICATION_CREDENTIALS')
        ], 'gcp')
        SecretManagerClient client = context.getBean(SecretManagerClient)

        when:
        def result = Mono.from(client.getSecret(System.getenv('GCP_SECRET_MANAGER_IMPORT_PATH'))).block()

        then:
        result != null
        result.name == System.getenv('GCP_SECRET_MANAGER_IMPORT_PATH')
        new String(result.contents, 'UTF-8').contains('application.debug=true')

        cleanup:
        context.close()
    }
}
