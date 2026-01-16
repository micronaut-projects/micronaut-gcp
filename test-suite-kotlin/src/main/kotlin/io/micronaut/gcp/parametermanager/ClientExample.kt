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
import io.micronaut.runtime.event.annotation.EventListener
//end::imports[]

//tag::clazz[]
class ClientExample(private val client: ParameterManagerAccessClient) {
    @EventListener
    fun onStartup(event: StartupEvent) {
        val parameter = client.getParameter("parameterName", "parameterVersion") // <1>
        val otherProjectParameter = client.getParameter("parameterName", "v1", "other-project-id") // <2>
        val renderedParameter = client.getRenderedParameter("parameterName", "parameterVersion") // <3>
        val otherProjectRenderedParameter = client.getRenderedParameter("parameterName", "v1", "other-project-id") // <4>
    }
}
//end::clazz[]
