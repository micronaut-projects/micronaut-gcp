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
import io.micronaut.context.env.PropertySourceImporter
import io.micronaut.core.convert.value.ConvertibleValues
import io.micronaut.core.io.service.SoftServiceLoader
import io.micronaut.core.util.ConnectionString
import spock.lang.Specification

class SecretManagerPropertySourceImporterSpec extends Specification {

    void "registers the gcp secret manager importer"() {
        when:
        List<PropertySourceImporter> importers = SoftServiceLoader.load(PropertySourceImporter).collectAll().asList()
        PropertySourceImporter importer = importers.find { it.provider == SecretManagerPropertySourceImporter.PROVIDER }

        then:
        importer instanceof SecretManagerPropertySourceImporter
    }

    void "supports scalar connection string declarations without bootstrap config client"() {
        given:
        ApplicationContext context = ApplicationContext.run([:])
        PropertySourceImporter importer = SoftServiceLoader.load(PropertySourceImporter)
                .collectAll()
                .asList()
                .find { it.provider == SecretManagerPropertySourceImporter.PROVIDER }

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('gcp-secret-manager://application'))

        then:
        declaration.path() == 'application'
        !declaration.optional()
        declaration.format() == null
        context != null

        cleanup:
        context.close()
    }

    void "supports structured declarations"() {
        given:
        PropertySourceImporter importer = new SecretManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConvertibleValues.of([
                provider  : 'gcp-secret-manager',
                path      : 'application_test',
                optional  : true,
                format    : 'yml',
                'project-id': 'first-gcp-project'
        ]))

        then:
        declaration.path() == 'application_test'
        declaration.optional()
        declaration.format() == 'yml'
        declaration.projectId() == 'first-gcp-project'
    }

    void "rejects blank paths"() {
        given:
        PropertySourceImporter importer = new SecretManagerPropertySourceImporter()

        when:
        importer.newImportDeclaration(ConvertibleValues.of([provider: 'gcp-secret-manager', path: '']))

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Google Secret Manager config import path cannot be blank'
    }
}
