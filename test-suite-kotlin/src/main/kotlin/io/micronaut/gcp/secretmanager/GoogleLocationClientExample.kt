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

//tag::imports[]
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
//end::imports[]

//tag::clazz[]
class GoogleLocationClientExample (private val client: SecretManagerServiceClient) { // <1>
	@EventListener
	fun onStartup(event: StartupEvent) {
		val response = client.accessSecretVersion(AccessSecretVersionRequest
				.newBuilder()
				.setName(SecretVersionName.ofProjectLocationSecretSecretVersionName("my-cloud-project", "us-central1", "secretName", "latest").toString())
				.build())
		val secret = response.payload.data.toStringUtf8()
	}
}
//end::clazz[]
