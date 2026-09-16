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
package example.cloudevents;

import com.google.events.cloud.storage.v1.StorageObjectData;
import io.cloudevents.CloudEventContext;
import io.micronaut.gcp.function.cloudevents.GoogleCloudEventsFunction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Example extends GoogleCloudEventsFunction<StorageObjectData> { // <1>

    @Override
    protected void accept(@NonNull CloudEventContext context, @Nullable StorageObjectData data) throws Exception { // <2>
        // process the storage object data
    }
}
