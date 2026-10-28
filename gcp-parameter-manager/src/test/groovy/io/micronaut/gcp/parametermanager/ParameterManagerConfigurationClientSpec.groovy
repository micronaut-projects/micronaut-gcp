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

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import io.micronaut.context.env.PropertySourceLoader
import io.micronaut.gcp.parametermanager.client.ParameterManagerAccessClient
import io.micronaut.gcp.parametermanager.client.VersionedParameter
import io.micronaut.gcp.parametermanager.configuration.ParameterManagerConfigurationProperties
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

class ParameterManagerConfigurationClientSpec extends Specification {

    private static VersionedParameter parameter(String name, String version, String text) {
        new VersionedParameter('first-gcp-project', 'global', name, version, text.getBytes('UTF-8'))
    }

    private ParameterManagerAccessClient access(Map<String, String> payloads) {
        Stub(ParameterManagerAccessClient) {
            getRenderedParameter(_ as String, _ as String) >> { args ->
                Mono.just(parameter(args[0], args[1], payloads[args[0]]))
            }
        }
    }

    private PropertySourceLoader loaderReading(String key, String value) {
        Stub(PropertySourceLoader) {
            read(_ as String, _ as byte[]) >> [(key): value]
        }
    }

    private Environment environmentWith(PropertySourceLoader loader) {
        Stub(Environment) {
            getPropertySourceLoaders() >> [loader]
        }
    }

    void "each load uses the readers of its own environment, even when subscribed later"() {
        given:
        def properties = new ParameterManagerConfigurationProperties()
        properties.customConfigs = ['cfg/v1'] as Set
        def clientA = new ParameterManagerConfigurationClient(access(['cfg': 'value: a']), properties)
        def clientB = new ParameterManagerConfigurationClient(access(['cfg': 'value: b']), properties)
        def envA = environmentWith(loaderReading('viaA', 'a'))
        def envB = environmentWith(loaderReading('viaB', 'b'))

        when: "both loads are started before either is subscribed to"
        def publisherA = clientA.getPropertySources(envA)
        def publisherB = clientB.getPropertySources(envB)
        def sourcesB = Flux.from(publisherB).collectList().block()
        def sourcesA = Flux.from(publisherA).collectList().block()
        def configA = sourcesA.find { it.name.startsWith('cfg') }
        def configB = sourcesB.find { it.name.startsWith('cfg') }

        then:
        configA.get('viaA') == 'a'
        configA.get('viaB') == null
        configB.get('viaB') == 'b'
        configB.get('viaA') == null
    }

    void "key values keep embedded newlines and surrounding whitespace"() {
        given:
        def properties = new ParameterManagerConfigurationProperties()
        properties.keys = ['DB_PASSWORD'] as Set
        def client = new ParameterManagerConfigurationClient(access(['DB_PASSWORD': '  first\nsecond  \n']), properties)

        when:
        def sources = Flux.from(client.getPropertySources(environmentWith(loaderReading('unused', 'x')))).collectList().block()
        def keys = sources.find { it.name == 'parameter-manager-keys' }

        then:
        keys.get('pm.DB_PASSWORD') == '  first\nsecond  \n'
    }

    void "key property names are normalised while the value is kept as stored"() {
        expect:
        ParameterManagerConfigurationClient.keyPropertyName(name) == 'pm.DB_PASSWORD'
        ParameterManagerConfigurationClient.keyPropertyValue(parameter('DB_PASSWORD', 'v1', value)) == value

        where:
        name       | value
        'DB_PASSWORD' | 'secret'
        'dbPassword'  | 'line one\nline two\n'
        'DB_PASSWORD' | ' padded '
    }

    void "key property lookup does not depend on the JVM default locale"() {
        given:
        Locale original = Locale.getDefault()

        when:
        Locale.setDefault(Locale.forLanguageTag('tr-TR'))
        String property
        Optional<String> looked
        ApplicationContext context
        try {
            property = ParameterManagerConfigurationClient.keyPropertyName('apiKey')
            context = ApplicationContext.run()
            context.environment.addPropertySource(PropertySource.of('keys', [(property): 'secret'], PropertySource.PropertyConvention.ENVIRONMENT_VARIABLE))
            looked = context.environment.getProperty('pm.api.key', String)
        } finally {
            Locale.setDefault(original)
            context?.close()
        }

        then:
        property == 'pm.API_KEY'
        looked.orElse(null) == 'secret'
    }
}
