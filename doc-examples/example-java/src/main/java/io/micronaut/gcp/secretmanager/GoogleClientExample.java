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
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
//end::imports[]

//tag::clazz[]
public final class GoogleClientExample {

    private final SecretManagerServiceClient client;

    public GoogleClientExample(SecretManagerServiceClient googleSecretManagerClient) { // <1>
        this.client = googleSecretManagerClient;
    }

    @EventListener
    public void onStartup(StartupEvent event) {
        AccessSecretVersionResponse response = client.accessSecretVersion(AccessSecretVersionRequest
                .newBuilder()
                .setName(SecretVersionName.of("my-cloud-project", "secretName", "latest").toString())
                .build());
        String secret = response.getPayload().getData().toStringUtf8();
    }
}
//end::clazz[]
