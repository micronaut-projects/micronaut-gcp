package io.micronaut.gcp.pubsub.push;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import jakarta.inject.Singleton;

@Singleton
@Introspected
public class PushMessageValidator implements ConstraintValidator<ValidPushMessage, PushRequest.PushMessage> {

    @Override
    public boolean isValid(PushRequest.@Nullable PushMessage value, @NonNull AnnotationValue<ValidPushMessage> annotationMetadata, @NonNull ConstraintValidatorContext context) {
        return StringUtils.isNotEmpty(value.data()) || (value.attributes() != null && !value.attributes().isEmpty());
    }
}
