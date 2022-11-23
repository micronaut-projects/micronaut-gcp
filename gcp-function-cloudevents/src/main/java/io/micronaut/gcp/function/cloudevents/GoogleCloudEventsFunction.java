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

import com.google.cloud.functions.CloudEventsFunction;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventContext;
import io.micronaut.cloudevents.CloudEventMapper;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.gcp.function.GoogleFunctionInitializer;

/**
 * A super class that extends {@link GoogleFunctionInitializer} and can be used to map cloud events payloads. To be used with CloudEvents POJOs issued by Google.
 * @see <a href="https://github.com/googleapis/google-cloudevents-java">Google Cloud Events</a>.
 *
 * @author Guillermo Calvo
 * @since 4.7.0
 */
public abstract class GoogleCloudEventsFunction<T> extends GoogleFunctionInitializer implements CloudEventsFunction {
    private final CloudEventMapper cloudEventMapper;

    protected GoogleCloudEventsFunction() {
         cloudEventMapper = this.applicationContext.getBean(CloudEventMapper.class);
    }

  @Override
  public void accept(CloudEvent event) throws Exception {
        accept(event, cloudEventMapper.map(event, getType()).orElse(null));
  }

  protected abstract Class<T> getType();

    //TODO add java doc similar to accept javadoc
   protected abstract void accept(@NonNull CloudEventContext context, @Nullable T data) throws Exception;
}
