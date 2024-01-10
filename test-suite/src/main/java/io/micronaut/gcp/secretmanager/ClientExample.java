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
package io.micronaut.gcp.secretmanager;
//tag::imports[]
import io.micronaut.context.event.StartupEvent;
import io.micronaut.gcp.secretmanager.client.SecretManagerClient;
import io.micronaut.gcp.secretmanager.client.VersionedSecret;
import io.micronaut.runtime.event.annotation.EventListener;
import reactor.core.publisher.Mono;
//end::imports[]

//tag::clazz[]
public final class ClientExample {

    private final SecretManagerClient client;

    public ClientExample(SecretManagerClient client) {
        this.client = client;
    }

    @EventListener
    public void onStartup(StartupEvent event) {
        Mono<VersionedSecret> secret = Mono.from(client.getSecret("secretId")); // <1>
        Mono<VersionedSecret> v2 = Mono.from(client.getSecret("secretId", "v2")); //<2>
        Mono<VersionedSecret> fromOtherProject = Mono.from(client.getSecret("secretId", "latest", "another-project-id")); //<3>
    }
}
//end::clazz[]
