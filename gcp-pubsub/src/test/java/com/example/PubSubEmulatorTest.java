package com.example;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PubSubEmulatorTest {

    public static void main(String[] args) {
        PubSubEmulatorTest test = new PubSubEmulatorTest();
        Publisher publisher = test.createPublisher();
        try {
            String ack = publisher.publish(PubsubMessage.newBuilder()
                    .setData(ByteString.copyFrom("Hello".getBytes()))
                    .build()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        publisher.shutdown();
    }



    public Publisher createPublisher() {
        try {
            ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8085").usePlaintext().build();
            TransportChannelProvider channelProvider =
                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

            // Set the channel and credentials provider when creating a `TopicAdminClient`.
            // Similarly for SubscriptionAdminClient
            TopicAdminClient topicClient =
                    TopicAdminClient.create(
                            TopicAdminSettings.newBuilder()
                                    .setTransportChannelProvider(channelProvider)
                                    .setCredentialsProvider(credentialsProvider)
                                    .build());

            TopicName topicName = TopicName.of("test-project", "my-topic-id");
            topicClient.createTopic(topicName);
            // Set the channel and credentials provider when creating a `Publisher`.
            // Similarly for Subscriber
            Publisher publisher =
                    Publisher.newBuilder(topicName)
                            .setChannelProvider(channelProvider)
                            .setCredentialsProvider(credentialsProvider)
                            .build();
            return publisher;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
