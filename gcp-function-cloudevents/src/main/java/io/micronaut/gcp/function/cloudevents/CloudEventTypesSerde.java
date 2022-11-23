/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.gcp.function.cloudevents;

import com.google.events.cloud.storage.v1.CustomerEncryption;
import com.google.events.cloud.storage.v1.StorageObjectData;
import io.micronaut.serde.annotation.SerdeImport;

import com.google.events.cloud.storage.v1.StorageObjectData;
import io.micronaut.serde.annotation.SerdeImport;

/**
 * @see <a href="https://github.com/googleapis/google-cloudevents-java">Google Cloud Events</a>.
 * @author Sergio del Amo
 * @since 4.8.0
 */
//TODO add all other classes
@SerdeImport(CustomerEncryption.class)
@SerdeImport(StorageObjectData.class)
public class CloudEventTypesSerde {
}
