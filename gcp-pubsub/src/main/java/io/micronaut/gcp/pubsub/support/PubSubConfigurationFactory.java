package io.micronaut.gcp.pubsub.support;

import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import org.threeten.bp.Duration;

import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Factory class to create default settings for PubSub Publisher and subscriber beans.
 *
 * @author Vinicius Carvalho
 */
@Factory
@Requires(classes = Publisher.class)
public class PubSubConfigurationFactory {

    private final PubSubConfigurationProperties pubSubConfigurationProperties;

    private final GoogleCloudConfiguration googleCloudConfiguration;

    public PubSubConfigurationFactory(PubSubConfigurationProperties pubSubConfigurationProperties, GoogleCloudConfiguration googleCloudConfiguration) {
        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
        this.googleCloudConfiguration = googleCloudConfiguration;
    }

    /**
     * @return Default retrySettings
     */
    @Singleton
    protected RetrySettings retrySettings() {
        PubSubConfigurationProperties.Retry retryProperties = this.pubSubConfigurationProperties.getPublisher().getRetry();
        RetrySettings.Builder builder = RetrySettings.newBuilder();
        RetrySettings settings = ifNotNull(retryProperties.getInitialRetryDelaySeconds(),
                (x) -> builder.setInitialRetryDelay(Duration.ofSeconds(x)))
                .apply(ifNotNull(retryProperties.getInitialRpcTimeoutSeconds(),
                        (x) -> builder.setInitialRpcTimeout(Duration.ofSeconds(x)))
                .apply(ifNotNull(retryProperties.getJittered(), builder::setJittered)
                .apply(ifNotNull(retryProperties.getMaxAttempts(), builder::setMaxAttempts)
                .apply(ifNotNull(retryProperties.getMaxRetryDelaySeconds(),
                        (x) -> builder.setMaxRetryDelay(Duration.ofSeconds(x)))
                .apply(ifNotNull(retryProperties.getMaxRpcTimeoutSeconds(),
                        (x) -> builder.setMaxRpcTimeout(Duration.ofSeconds(x)))
                .apply(ifNotNull(retryProperties.getRetryDelayMultiplier(), builder::setRetryDelayMultiplier)
                .apply(ifNotNull(retryProperties.getTotalTimeoutSeconds(),
                        (x) -> builder.setTotalTimeout(Duration.ofSeconds(x)))
                .apply(ifNotNull(retryProperties.getRpcTimeoutMultiplier(), builder::setRpcTimeoutMultiplier)
                .apply(false))))))))) ? builder.build() : null;
        return settings;
    }

    /**
     *
     * @return batchSettings
     */
    @Singleton
    public BatchingSettings publisherBatchSettings() {
        BatchingSettings.Builder builder = BatchingSettings.newBuilder();

        PubSubConfigurationProperties.Batching batching = this.pubSubConfigurationProperties.getPublisher()
                .getBatching();

        FlowControlSettings flowControlSettings = buildFlowControlSettings(batching.getFlowControl());
        if (flowControlSettings != null) {
            builder.setFlowControlSettings(flowControlSettings);
        }

        return ifNotNull(batching.getDelayThresholdSeconds(),
                (x) -> builder.setDelayThreshold(Duration.ofSeconds(x)))
                .apply(ifNotNull(batching.getElementCountThreshold(), builder::setElementCountThreshold)
                 .apply(ifNotNull(batching.getEnabled(), builder::setIsEnabled)
                 .apply(ifNotNull(batching.getRequestByteThreshold(), builder::setRequestByteThreshold)
                 .apply(false)))) ? builder.build() : null;
    }

    /**
     *
     * @return default {@link ExecutorProvider}
     */
    @Singleton
    public ExecutorProvider publisherExecutorProvider() {
        //TODO needs to provide better scheduled executor
        return FixedExecutorProvider.create(Executors.newScheduledThreadPool(1));
    }

    /**
     *
     * @return default {@link TransportChannelProvider}ansportChannelProvider
     */
    @Singleton
    public TransportChannelProvider transportChannelProvider() {
        return InstantiatingGrpcChannelProvider.newBuilder()
                .setKeepAliveTime(Duration.ofMinutes(this.pubSubConfigurationProperties.getKeepAliveIntervalMinutes()))
                .build();
    }

    private FlowControlSettings buildFlowControlSettings(PubSubConfigurationProperties.FlowControl flowControl) {
        FlowControlSettings.Builder builder = FlowControlSettings.newBuilder();

        return ifNotNull(flowControl.getLimitExceededBehavior(), builder::setLimitExceededBehavior)
                .apply(ifNotNull(flowControl.getMaxOutstandingElementCount(),
                        builder::setMaxOutstandingElementCount)
                .apply(ifNotNull(flowControl.getMaxOutstandingRequestBytes(),
                        builder::setMaxOutstandingRequestBytes)
                .apply(false))) ? builder.build() : null;
    }
    /**
     * A helper method for applying properties to settings builders for purpose of seeing if at least
     * one setting was set.
     *
     * @param prop     the property on which to operate
     * @param consumer the function to give the property
     * @param <T>      the type of the property
     * @return a function that accepts a boolean of if there is a next property and returns a boolean indicating if the
     * propety was set
     */
    private <T> Function<Boolean, Boolean> ifNotNull(T prop, Consumer<T> consumer) {
        return (next) -> {
            boolean wasSet = next;
            if (prop != null) {
                consumer.accept(prop);
                wasSet = true;
            }
            return wasSet;
        };
    }
}
