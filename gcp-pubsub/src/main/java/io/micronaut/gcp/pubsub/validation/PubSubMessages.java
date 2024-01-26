/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.gcp.pubsub.validation;

import io.micronaut.context.StaticMessageSource;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;

/**
 * Validation messages for GCP PubSub.
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
@Singleton
@Internal
final class PubSubMessages extends StaticMessageSource {

    /**
     * {@link ValidPushMessage} message.
     */
    public static final String VALID_PUSH_MESSAGE = "invalid pubsub push request message ({validatedValue}) - message must contain either a non-empty data field or at least one attribute";

    /**
     * The message suffix to use.
     */
    private static final String MESSAGE_SUFFIX = ".message";

    /**
     * Default constructor to initialize messages.
     * via {@link #addMessage(String, String)}
     */
    public PubSubMessages() {
        addMessage(ValidPushMessage.class.getName() + MESSAGE_SUFFIX, VALID_PUSH_MESSAGE);
    }
}
