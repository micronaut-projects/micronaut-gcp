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
package io.micronaut.gcp.pubsub.exception;

import io.micronaut.core.exceptions.ExceptionHandler;

/**
 * Marker interface that {@link io.micronaut.gcp.pubsub.annotation.PubSubListener} beans can implement
 * to handle exceptions.
 *
 * Implementations of this interface are responsible for deciding whether to ack/nack the message and should do so
 * via the supplied {@link com.google.cloud.pubsub.v1.AckReplyConsumer} that can be retrieved via the exception's
 * {@link io.micronaut.gcp.pubsub.bind.PubSubConsumerState}.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public interface PubSubMessageReceiverExceptionHandler extends ExceptionHandler<PubSubMessageReceiverException> {
}
