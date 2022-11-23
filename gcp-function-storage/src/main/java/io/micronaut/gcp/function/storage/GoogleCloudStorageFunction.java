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
package io.micronaut.gcp.function.storage;

import com.google.cloud.functions.CloudEventsFunction;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.gcp.function.storage.cloudevents.CloudEventMapper;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * A super class that extends {@link GoogleFunctionInitializer} and can be used to receive GCP storage events.
 *
 * @author Guillermo Calvo
 * @since XXXX
 */
public abstract class GoogleCloudStorageFunction extends GoogleFunctionInitializer
    implements CloudEventsFunction {

  @Inject
  @Named("google-storage-object-mapper")
  protected CloudEventMapper<GoogleStorageObject> cloudEventMapper;

  @Override
  public void accept(CloudEvent event) throws Exception {
    this.accept(event, this.cloudEventMapper.map(event).orElse(null));
  }

  public abstract void accept(
      @NonNull CloudEventContext context, @Nullable GoogleStorageObject data) throws Exception;
}
