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
//tag::imports[]
import io.micronaut.context.event.StartupEvent
import io.micronaut.gcp.parametermanager.client.ParameterManagerAccessClient
import io.micronaut.gcp.parametermanager.client.VersionedParameter
import io.micronaut.runtime.event.annotation.EventListener
import reactor.core.publisher.Mono
//end::imports[]

//tag::clazz[]
class ClientExample {

    private final ParameterManagerAccessClient client

    ClientExample(ParameterManagerAccessClient client) {
        this.client = client
    }

    @EventListener
    void onStartup(StartupEvent event) {
        Mono<VersionedParameter> parameter = client.getParameter("parameterName", "parameterVersion") // <1>
        Mono<VersionedParameter> otherProjectParameter = client.getParameter("parameterName", "v1", "other-project-id") // <2>
        Mono<VersionedParameter> renderedParameter = client.getRenderedParameter("parameterName", "parameterVersion") // <3>
        Mono<VersionedParameter> otherProjectRenderedParameter = client.getRenderedParameter("parameterName", "v1", "other-project-id") // <4>
    }
}
//end::clazz[]
