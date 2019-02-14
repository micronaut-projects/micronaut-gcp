package io.micronaut.gcp.condition;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.gcp.GoogleCloudConfiguration;

public class RequiresProjectIdCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context) {
        final GoogleCloudConfiguration cloudConfiguration = context.getBeanContext().getBean(GoogleCloudConfiguration.class);
        if (!cloudConfiguration.hasProjectId()) {
            context.fail(GoogleCloudConfiguration.NO_PROJECT_ID_MESSAGE);
            return false;
        }
        return true;
    }
}
