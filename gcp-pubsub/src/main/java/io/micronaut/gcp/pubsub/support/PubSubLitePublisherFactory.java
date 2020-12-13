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
package io.micronaut.gcp.pubsub.support;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.PublisherInterface;
import com.google.cloud.pubsublite.ProjectId;
import com.google.cloud.pubsublite.TopicName;
import com.google.cloud.pubsublite.TopicPath;
import com.google.cloud.pubsublite.cloudpubsub.PublisherSettings;
import com.google.cloud.pubsublite.cloudpubsub.Publisher;
import io.micronaut.context.BeanContext;
import io.micronaut.gcp.Modules;

import javax.inject.Named;

/**
 * Factory for Pub/Sub lite Publishers.
 */
public class PubSubLitePublisherFactory implements PublisherFactory {

    private final TransportChannelProvider transportChannelProvider;
    private final CredentialsProvider credentialsProvider;
    private final BeanContext beanContext;

    public PubSubLitePublisherFactory(@Named(Modules.PUBSUB) TransportChannelProvider transportChannelProvider,
                                   @Named(Modules.PUBSUB) CredentialsProvider credentialsProvider,
                                   BeanContext beanContext) {
        this.transportChannelProvider = transportChannelProvider;
        this.credentialsProvider = credentialsProvider;
        this.beanContext = beanContext;
    }

    @Override
    public PublisherInterface createPublisher(PublisherFactoryConfig config) {
        TopicPath topicPath = TopicPath.newBuilder()
                .setProject(ProjectId.of(config.getTopicState().getProjectTopicName().getProject()))
                .setName(TopicName.of(config.getTopicState().getProjectTopicName().getTopic()))
                .build();
        PublisherSettings settings = PublisherSettings.newBuilder()
                .setTopicPath(topicPath)

                .build();
        return Publisher.create(settings);
    }
}
