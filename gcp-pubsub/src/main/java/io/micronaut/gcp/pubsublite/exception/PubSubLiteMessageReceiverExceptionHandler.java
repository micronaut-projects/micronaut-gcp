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
package io.micronaut.gcp.pubsublite.exception;

import io.micronaut.core.exceptions.ExceptionHandler;

/**
 * Marker interface that {@link io.micronaut.gcp.pubsublite.annotation.PubSubLiteListener} beans can implement
 * to handle exceptions.
 *
 * Based on {@link io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler}
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
public interface PubSubLiteMessageReceiverExceptionHandler extends ExceptionHandler<PubSubLiteMessageReceiverException> {
}
