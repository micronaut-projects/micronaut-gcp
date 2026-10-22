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
package io.micronaut.gcp.parametermanager.imports

import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutures
import com.google.api.gax.grpc.GrpcStatusCode
import com.google.api.gax.rpc.ApiCallContext
import com.google.api.gax.rpc.ApiException
import com.google.api.gax.rpc.NotFoundException
import com.google.api.gax.rpc.UnaryCallable
import com.google.cloud.parametermanager.v1.ParameterManagerClient
import com.google.cloud.parametermanager.v1.stub.ParameterManagerStub
import com.google.cloud.parametermanager.v1.RenderParameterVersionRequest
import com.google.cloud.parametermanager.v1.RenderParameterVersionResponse
import com.google.protobuf.ByteString
import io.grpc.Status
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySourceImporter
import io.micronaut.core.convert.value.ConvertibleValues
import io.micronaut.core.io.service.SoftServiceLoader
import io.micronaut.core.util.ConnectionString
import io.micronaut.gcp.parametermanager.configuration.ParameterManagerConfigurationProperties
import org.jspecify.annotations.Nullable
import spock.lang.Specification

class ParameterManagerPropertySourceImporterSpec extends Specification {

    void "registers the gcp parameter manager importer"() {
        when:
        List<PropertySourceImporter> importers = SoftServiceLoader.load(PropertySourceImporter).collectAll().asList()
        PropertySourceImporter importer = importers.find { it.provider == ParameterManagerPropertySourceImporter.PROVIDER }

        then:
        importer instanceof ParameterManagerPropertySourceImporter
    }

    void "registers the gcp parameter manager importer for application context bootstrap discovery"() {
        when:
        ApplicationContext context = ApplicationContext.run([:])
        List<PropertySourceImporter> importers = SoftServiceLoader.load(PropertySourceImporter, context.classLoader).collectAll().asList()
        PropertySourceImporter importer = importers.find { it.provider == ParameterManagerPropertySourceImporter.PROVIDER }

        then:
        importer instanceof ParameterManagerPropertySourceImporter

        cleanup:
        context.close()
    }

    void "supports scalar connection string declarations without bootstrap config client"() {
        given:
        ApplicationContext context = ApplicationContext.run([:])
        PropertySourceImporter importer = SoftServiceLoader.load(PropertySourceImporter)
                .collectAll()
                .asList()
                .find { it.provider == ParameterManagerPropertySourceImporter.PROVIDER }

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('gcp-parameter-manager://application?project-id=my-gcp-project&version=1'))

        then:
        declaration.declaration().path() == 'application'
        !declaration.declaration().optional()
        declaration.declaration().format() == null
        declaration.declaration().projectId() == 'my-gcp-project'
        declaration.declaration().version() == '1'
        declaration.retryPolicy().maxAttempts() == 3
        context != null

        cleanup:
        context.close()
    }

    void "supports scalar connection string declarations with credentials location in query parameters"() {
        given:
        PropertySourceImporter importer = new ParameterManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('gcp-parameter-manager://application?project-id=my-gcp-project&version=1&credentials-location=credentials-file-path'))

        then:
        declaration.declaration().path() == 'application'
        declaration.declaration().credentialsLocation() == 'credentials-file-path'
        declaration.declaration().encodedKey() == null
        declaration.declaration().projectId() == 'my-gcp-project'
    }

    void "supports structured declarations"() {
        given:
        PropertySourceImporter importer = new ParameterManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConvertibleValues.of([
                provider  : 'gcp-parameter-manager',
                path      : 'application_test',
                optional  : true,
                format    : 'yml',
                version   : '3',
                'project-id': 'first-gcp-project'
        ]))

        then:
        declaration.declaration().path() == 'application_test'
        declaration.declaration().optional()
        declaration.declaration().format() == 'yml'
        declaration.declaration().projectId() == 'first-gcp-project'
        declaration.declaration().version() == '3'
        declaration.retryPolicy().maxAttempts() == 3
    }

    void "rejects blank paths"() {
        given:
        PropertySourceImporter importer = new ParameterManagerPropertySourceImporter()

        when:
        importer.newImportDeclaration(ConvertibleValues.of([provider: 'gcp-parameter-manager', path: '', version: '1']))

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Google Cloud Parameter Manager config import path cannot be blank'
    }

    void "defaults the version to latest when not specified"() {
        given:
        PropertySourceImporter importer = new ParameterManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConvertibleValues.of([provider: 'gcp-parameter-manager', path: 'application']))

        then:
        declaration.declaration().version() == 'latest'
    }

    void "rejects declarations with both credentials-location and encoded-key"() {
        given:
        PropertySourceImporter importer = new ParameterManagerPropertySourceImporter()

        when:
        importer.newImportDeclaration(ConvertibleValues.of([
                provider              : 'gcp-parameter-manager',
                path                  : 'application',
                version               : '1',
                'project-id'          : 'my-gcp-project',
                'credentials-location': '/path/to/sa.json',
                'encoded-key'         : 'dGVzdA=='
        ]))

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Please specify only one of credentials-location or encoded-key for gcp-parameter-manager imports'
    }

    void "allows declarations without project-id for later environment resolution"() {
        given:
        PropertySourceImporter importer = new ParameterManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConvertibleValues.of([
                provider: 'gcp-parameter-manager',
                path    : 'application',
                version : '1'
        ]))

        then:
        declaration.declaration().projectId() == null
    }

    void "normalises path with leading slash from triple-slash URI"() {
        given:
        PropertySourceImporter importer = new ParameterManagerPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('gcp-parameter-manager:///application?project-id=my-gcp-project&version=1'))

        then:
        declaration.declaration().path() == 'application'
    }

    void "optional import of a missing parameter version is omitted"() {
        given:
        def declaration = importDeclaration(optional: true)

        when:
        def result = ParameterManagerPropertySourceImporter.renderParameter(clientRendering(notFound()), declaration, new ParameterManagerConfigurationProperties())

        then:
        result.isEmpty()
    }

    void "required import of a missing parameter version still fails"() {
        given:
        def declaration = importDeclaration([:])

        when:
        ParameterManagerPropertySourceImporter.renderParameter(clientRendering(notFound()), declaration, new ParameterManagerConfigurationProperties())

        then:
        thrown(NotFoundException)
    }

    void "optional import of an existing parameter version is rendered"() {
        given:
        def declaration = importDeclaration(optional: true)

        when:
        def result = ParameterManagerPropertySourceImporter.renderParameter(clientRendering(null), declaration, new ParameterManagerConfigurationProperties())

        then:
        result.isPresent()
        result.get().name == 'application'
        new String(result.get().contents) == 'key: value'
    }

    void "per-import encoded key overrides a global credentials location"() {
        given:
        ApplicationContext context = ApplicationContext.run(['gcp.credentials.location': '/global/sa.json'])
        def declaration = importDeclaration(['encoded-key': 'ZW5jb2RlZA=='])

        when:
        def resolved = ParameterManagerPropertySourceImporter.resolveDeclaration(context.environment, declaration)

        then:
        resolved.encodedKey() == 'ZW5jb2RlZA=='
        resolved.credentialsLocation() == null

        cleanup:
        context.close()
    }

    void "per-import credentials location overrides a global encoded key"() {
        given:
        ApplicationContext context = ApplicationContext.run(['gcp.credentials.encoded-key': 'Z2xvYmFs'])
        def declaration = importDeclaration(['credentials-location': '/import/sa.json'])

        when:
        def resolved = ParameterManagerPropertySourceImporter.resolveDeclaration(context.environment, declaration)

        then:
        resolved.credentialsLocation() == '/import/sa.json'
        resolved.encodedKey() == null

        cleanup:
        context.close()
    }

    void "global credentials are used when the import supplies none"() {
        given:
        ApplicationContext context = ApplicationContext.run(['gcp.credentials.location': '/global/sa.json'])
        def declaration = importDeclaration([:])

        when:
        def resolved = ParameterManagerPropertySourceImporter.resolveDeclaration(context.environment, declaration)

        then:
        resolved.credentialsLocation() == '/global/sa.json'
        resolved.encodedKey() == null

        cleanup:
        context.close()
    }

    void "global credentials specifying both modes are still rejected when the import supplies none"() {
        given:
        ApplicationContext context = ApplicationContext.run(['gcp.credentials.location': '/global/sa.json',
                                                             'gcp.credentials.encoded-key': 'Z2xvYmFs'])
        def declaration = importDeclaration([:])

        when:
        ParameterManagerPropertySourceImporter.resolveDeclaration(context.environment, declaration)

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Please specify only one of credentials-location or encoded-key for gcp-parameter-manager imports'

        cleanup:
        context.close()
    }

    private static ParameterManagerImportDeclaration importDeclaration(Map values) {
        Map all = [provider: 'gcp-parameter-manager', path: 'application', version: '1', 'project-id': 'my-gcp-project'] + values
        new ParameterManagerPropertySourceImporter().newImportDeclaration(ConvertibleValues.of(all)).declaration()
    }

    private static ApiException notFound() {
        new NotFoundException('parameter version not found', null, GrpcStatusCode.of(Status.Code.NOT_FOUND), false)
    }

    private ParameterManagerClient clientRendering(@Nullable ApiException failure) {
        ParameterManagerStub stub = Stub(ParameterManagerStub) {
            renderParameterVersionCallable() >> new UnaryCallable<RenderParameterVersionRequest, RenderParameterVersionResponse>() {
                @Override
                ApiFuture<RenderParameterVersionResponse> futureCall(RenderParameterVersionRequest request, ApiCallContext context) {
                    if (failure != null) {
                        return ApiFutures.immediateFailedFuture(failure)
                    }
                    ApiFutures.immediateFuture(RenderParameterVersionResponse.newBuilder()
                        .setRenderedPayload(ByteString.copyFromUtf8('key: value'))
                        .build())
                }
            }
        }
        ParameterManagerClient.create(stub)
    }
}
