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
package io.micronaut.gcp.pubsub.bind;
//tag::imports[]
import com.google.protobuf.util.Timestamps;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;

import jakarta.inject.Singleton;
//end::imports[]

// tag::clazz[]
@Singleton // <1>
public class MessagePublishTimeAnnotationBinder implements PubSubAnnotatedArgumentBinder<MessagePublishTime> { // <2>

    private final ConversionService conversionService;

    public MessagePublishTimeAnnotationBinder(ConversionService conversionService) { // <3>
        this.conversionService = conversionService;
    }

    @Override
    public Class<MessagePublishTime> getAnnotationType() {
        return MessagePublishTime.class;
    }

    @Override
    public BindingResult<Object> bind(ArgumentConversionContext<Object> context, PubSubConsumerState source) {
        Long epochMillis = Timestamps.toMillis(source.getPubsubMessage().getPublishTime()); // <4>
        return () -> conversionService.convert(epochMillis, context); // <5>
    }
}
// end::clazz[]
