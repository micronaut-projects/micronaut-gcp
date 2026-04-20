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


    void "registers the gcp secret manager importer for application context bootstrap discovery"() {
        when:
        ApplicationContext context = ApplicationContext.run([:])
        List<PropertySourceImporter> importers = SoftServiceLoader.load(PropertySourceImporter, context.classLoader).collectAll().asList()
        PropertySourceImporter importer = importers.find { it.provider == SecretManagerPropertySourceImporter.PROVIDER }

        then:
        importer instanceof SecretManagerPropertySourceImporter

        cleanup:
        context.close()
    }

    void "supports scalar connection string declarations without bootstrap config client"() {
        given:
        ApplicationContext context = ApplicationContext.run([:])
        PropertySourceImporter importer = SoftServiceLoader.load(PropertySourceImporter)
                .collectAll()
                .asList()
                .find { it.provider == SecretManagerPropertySourceImporter.PROVIDER }

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('gcp-secret-manager://application?project-id=my-gcp-project'))

        then:
        declaration.declaration().path() == 'application'
        !declaration.declaration().optional()
        declaration.declaration().format() == null
        declaration.declaration().projectId() == 'my-gcp-project'
        declaration.declaration().version() == null
        declaration.retryPolicy().maxAttempts() == 3
        context != null

        cleanup:
        context.close()
    }


    void "supports scalar connection string declarations with explicit version"() {
        given:
        PropertySourceImporter importer = new SecretManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('gcp-secret-manager://application?project-id=my-gcp-project&version=5'))

        then:
        declaration.declaration().path() == 'application'
        declaration.declaration().projectId() == 'my-gcp-project'
        declaration.declaration().version() == '5'
    }

    void "supports scalar connection string declarations with credentials location in query parameters"() {
        given:
        PropertySourceImporter importer = new SecretManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('gcp-secret-manager://application?project-id=my-gcp-project&credentials-location=credentials-file-path'))

        then:
        declaration.declaration().path() == 'application'
        declaration.declaration().credentialsLocation() == 'credentials-file-path'
        declaration.declaration().encodedKey() == null
        declaration.declaration().projectId() == 'my-gcp-project'
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
        declaration.declaration().path() == 'application_test'
        declaration.declaration().optional()
        declaration.declaration().format() == 'yml'
        declaration.declaration().projectId() == 'first-gcp-project'
        declaration.declaration().version() == null
        declaration.retryPolicy().maxAttempts() == 3
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

    void "rejects declarations with both credentials-location and encoded-key"() {
        given:
        PropertySourceImporter importer = new SecretManagerPropertySourceImporter()

        when:
        importer.newImportDeclaration(ConvertibleValues.of([
                provider              : 'gcp-secret-manager',
                path                  : 'application',
                'project-id'          : 'my-gcp-project',
                'credentials-location': '/path/to/sa.json',
                'encoded-key'         : 'dGVzdA=='
        ]))

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Please specify only one of credentials-location or encoded-key for gcp-secret-manager imports'
    }

    void "allows declarations without project-id for later environment resolution"() {
        given:
        PropertySourceImporter importer = new SecretManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConvertibleValues.of([
                provider: 'gcp-secret-manager',
                path    : 'application'
        ]))

        then:
        declaration.declaration().projectId() == null
    }

    void "normalises path with leading slash from triple-slash URI"() {
        given:
        PropertySourceImporter importer = new SecretManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('gcp-secret-manager:///application?project-id=my-gcp-project'))

        then:
        declaration.declaration().path() == 'application'
    }
}
