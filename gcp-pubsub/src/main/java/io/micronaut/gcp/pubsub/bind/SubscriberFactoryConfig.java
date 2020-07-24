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

import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectSubscriptionName;

/**
 * Holds necessary configuration to create {@link com.google.cloud.pubsub.v1.Subscriber} @see {@link SubscriberFactory}.
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public class SubscriberFactoryConfig {

    private final ProjectSubscriptionName subscriptionName;
    private final MessageReceiver receiver;
    private final String subscriberConfiguration;
    private final String defaultExecutor;

    public SubscriberFactoryConfig(ProjectSubscriptionName subscriptionName, MessageReceiver receiver, String subscriberConfiguration, String defaultExecutor) {
        this.subscriptionName = subscriptionName;
        this.receiver = receiver;
        this.subscriberConfiguration = subscriberConfiguration;
        this.defaultExecutor = defaultExecutor;
    }

    /**
     *
     * @return ProjectSubscriptionName
     */
    public ProjectSubscriptionName getSubscriptionName() {
        return subscriptionName;
    }

    /**
     *
     * @return MessageReceiver to be registered for the created {@link com.google.cloud.pubsub.v1.Subscriber}
     */
    public MessageReceiver getReceiver() {
        return receiver;
    }

    /**
     *
     * @return name of the configuration passed via {@link io.micronaut.gcp.pubsub.annotation.Subscription}
     */
    public String getSubscriberConfiguration() {
        return subscriberConfiguration;
    }

    /**
     *
     * @return default {@link java.util.concurrent.ExecutorService} set for all subscribers.
     */
    public String getDefaultExecutor() {
        return defaultExecutor;
    }
}
