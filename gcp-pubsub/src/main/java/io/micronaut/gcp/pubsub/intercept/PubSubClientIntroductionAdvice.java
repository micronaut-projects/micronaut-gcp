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
package io.micronaut.gcp.pubsub.intercept;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.exception.PubSubClientException;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;
import io.micronaut.gcp.pubsub.support.PublisherFactory;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.messaging.annotation.Body;
import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author Vinicius Carvalho
 * @since 2.0
 */
@Singleton
public class PubSubClientIntroductionAdvice implements MethodInterceptor<Object, Object> {

    private final PublisherFactory publisherFactory;
    private final PubSubMessageSerDesRegistry serDesRegistry;
    private final Scheduler scheduler;
    private final ConversionService<?> conversionService;

    public PubSubClientIntroductionAdvice(PublisherFactory publisherFactory,
                                          PubSubMessageSerDesRegistry serDesRegistry,
                                          @Named(TaskExecutors.IO) ExecutorService executorService,
                                          ConversionService<?> conversionService) {
        this.publisherFactory = publisherFactory;
        this.serDesRegistry = serDesRegistry;
        this.scheduler = Schedulers.from(executorService);
        this.conversionService = conversionService;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {

        if (context.hasAnnotation(Topic.class)) {
            AnnotationValue<Topic> topic = context.getAnnotation(Topic.class);
            String contentType = topic.get("contentType", String.class).orElse("");
            PubSubMessageSerDes serDes = serDesRegistry.find(contentType)
                    .orElseThrow(() -> new PubSubClientException("Could not locate a valid SerDes implementation for type: " + contentType));
            Publisher publisher = publisherFactory.createPublisher(topic.getValue(String.class).orElse(""));
            Argument<?> bodyArgument = findBodyArgument(context.getExecutableMethod())
                    .orElseThrow(() -> new PubSubClientException("No valid message body argument found for method: " + context.getExecutableMethod()));
            Map<String, Object> parameterValues = context.getParameterValueMap();
            Map<String, String> messageAttributes = new HashMap<>();
            messageAttributes.put("Content-Type", contentType);
            Object body = parameterValues.get(bodyArgument.getName());
            ReturnType<Object> returnType = context.getReturnType();
            Class<?> javaReturnType = returnType.getType();

            byte[] serialized = (body.getClass() == byte[].class) ? (byte[]) body : serDes.serialize(body);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFrom(serialized))
                    .putAllAttributes(messageAttributes)
                    .build();
            ApiFuture<String> future = publisher.publish(pubsubMessage);
            Single<String> reactiveResult = Single.fromFuture(future).subscribeOn(this.scheduler);
            boolean isReactive = Publishers.isConvertibleToPublisher(javaReturnType);
            if (javaReturnType == void.class || javaReturnType == Void.class) {
                String result = reactiveResult.blockingGet();
                return null;
            } else {
                if (isReactive) {
                    return conversionService.convert(reactiveResult, javaReturnType)
                            .orElseThrow(() -> new PubSubClientException("Could not convert publisher result to method return type: " + javaReturnType));
                } else {
                    String result = reactiveResult.blockingGet();
                    return conversionService.convert(result, javaReturnType)
                            .orElseThrow(() -> new PubSubClientException("Could not convert publisher result to method return type: " + javaReturnType));
                }
            }
        } else {
            return context.proceed();
        }

    }

    private Optional<Argument<?>> findBodyArgument(ExecutableMethod<?, ?> method) {
        return Optional.ofNullable(Arrays.stream(method.getArguments())
                .filter(argument -> argument.getAnnotationMetadata().hasAnnotation(Body.class))
                .findFirst()
                .orElseGet(
                        () -> Arrays.stream(method.getArguments())
                        .findFirst()
                        .orElse(null)
                )
        );
    }
}
