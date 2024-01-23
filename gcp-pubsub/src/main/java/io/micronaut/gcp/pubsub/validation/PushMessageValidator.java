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

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.pubsub.push.PushRequest;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;

/**
 * A validator for incoming PubSub push messages.
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
@Introspected
public class PushMessageValidator implements ConstraintValidator<ValidPushMessage, PushRequest.PushMessage> {

    /**
     * Validates pub sub push messages, ensuring they meet the specified constraint that a message must contain either
     * a non-empty {@code data} field, or at least one non-empty {@code attribute}.
     *
     * @param value The push message to validate
     * @param annotationMetadata The annotation metadata
     * @param context The context object
     *
     * @return
     */
    @Override
    public boolean isValid(PushRequest.@Nullable PushMessage value, @NonNull AnnotationValue<ValidPushMessage> annotationMetadata, @NonNull ConstraintValidatorContext context) {
        return value != null && (StringUtils.isNotEmpty(value.data()) || (value.attributes() != null && !value.attributes().isEmpty()));
    }
}
