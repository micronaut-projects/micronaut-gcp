package io.micronaut.gcp.pubsub.configuration;

import com.google.api.gax.batching.FlowController;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.gcp.GoogleCloudConfiguration;
import javax.annotation.Nullable;

@ConfigurationProperties(PubSubConfigurationProperties.PREFIX)
@Context
public class PubSubConfigurationProperties {

    public static final String PREFIX = GoogleCloudConfiguration.PREFIX + ".pubsub";

    private  Publisher publisher = new Publisher();

    private int keepAliveIntervalMinutes = 5;

    /**
     * How often to ping the server to keep the channel alive.
     * @return interval
     */
    public int getKeepAliveIntervalMinutes() {
        return keepAliveIntervalMinutes;
    }

    /**
     * How often to ping the server to keep the channel alive.
     * @param keepAliveIntervalMinutes
     */
    public void setKeepAliveIntervalMinutes(int keepAliveIntervalMinutes) {
        this.keepAliveIntervalMinutes = keepAliveIntervalMinutes;
    }

    /**
     *
     * @return publisher configuration
     */
    public Publisher getPublisher() {
        return publisher;
    }

    /**
     *
     * @param publisher configuration
     */
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Publisher settings.
     */
    @ConfigurationProperties("publisher")
    public static class Publisher {

        private int executorThreads = 4;

        private  Retry retry = new Retry();

        private  Batching batching = new Batching();

        /**
         * Batching properties.
         * @return batching
         */
        public Batching getBatching() {
            return this.batching;
        }

        /**
         * Batching properties.
         * @param batching
         */
        public void setBatching(Batching batching) {
            this.batching = batching;
        }

        /**
         * Retry properties.
         * @return retry
         */
        public Retry getRetry() {
            return this.retry;
        }

        /**
         * Retry properties.
         * @param retry
         */
        public void setRetry(Retry retry) {
            this.retry = retry;
        }

        /**
         * Number of threads used by every publisher.
         * @return executorThreads
         */
        public int getExecutorThreads() {
            return this.executorThreads;
        }

        /**
         * Number of threads used by every publisher.
         * @param executorThreads
         */
        public void setExecutorThreads(int executorThreads) {
            this.executorThreads = executorThreads;
        }
    }

    /**
     * Retry settings.
     */
    @ConfigurationProperties("retry")
    public static class Retry {

        private Long totalTimeoutSeconds;

        private Long initialRetryDelaySeconds;

        private Double retryDelayMultiplier;

        private Long maxRetryDelaySeconds;

        private Integer maxAttempts;

        private Boolean jittered;

        private Long initialRpcTimeoutSeconds;

        private Double rpcTimeoutMultiplier;

        private Long maxRpcTimeoutSeconds;

        /**
         * TotalTimeout has ultimate control over how long the logic should keep trying the remote call
         * until it gives up completely. The higher the total timeout, the more retries can be
         * attempted.
         * @return timeout in seconds
         */
        public Long getTotalTimeoutSeconds() {
            return this.totalTimeoutSeconds;
        }

        /**
         * TotalTimeout has ultimate control over how long the logic should keep trying the remote call
         * until it gives up completely. The higher the total timeout, the more retries can be
         * attempted.
         * @param totalTimeoutSeconds timeout in seconds
         */
        public void setTotalTimeoutSeconds(@Nullable Long totalTimeoutSeconds) {
            this.totalTimeoutSeconds = totalTimeoutSeconds;
        }

        /**
         * InitialRetryDelay controls the delay before the first retry. Subsequent retries will use this
         * value adjusted according to the RetryDelayMultiplier.
         * @return initial retry delay in seconds
         */
        public Long getInitialRetryDelaySeconds() {
            return this.initialRetryDelaySeconds;
        }

        /**
         * InitialRetryDelay controls the delay before the first retry. Subsequent retries will use this
         * value adjusted according to the RetryDelayMultiplier.
         * @param initialRetryDelaySeconds retry delay in seconds
         */
        public void setInitialRetryDelaySeconds(Long initialRetryDelaySeconds) {
            this.initialRetryDelaySeconds = initialRetryDelaySeconds;
        }

        /**
         * RetryDelayMultiplier controls the change in retry delay. The retry delay of the previous call
         * is multiplied by the RetryDelayMultiplier to calculate the retry delay for the next call.
         * @return retryDelayMultiplier
         */
        public Double getRetryDelayMultiplier() {
            return this.retryDelayMultiplier;
        }

        /**
         * RetryDelayMultiplier controls the change in retry delay. The retry delay of the previous call
         * is multiplied by the RetryDelayMultiplier to calculate the retry delay for the next call.
         * @param  retryDelayMultiplier multiplier to set
         */
        public void setRetryDelayMultiplier(Double retryDelayMultiplier) {
            this.retryDelayMultiplier = retryDelayMultiplier;
        }

        /**
         * MaxRetryDelay puts a limit on the value of the retry delay, so that the RetryDelayMultiplier
         * can't increase the retry delay higher than this amount.
         * @return maxRetryDelaySeconds
         */
        public Long getMaxRetryDelaySeconds() {
            return this.maxRetryDelaySeconds;
        }

        /**
         * MaxRetryDelay puts a limit on the value of the retry delay, so that the RetryDelayMultiplier
         * can't increase the retry delay higher than this amount.
         * @param maxRetryDelaySeconds delay in seconds
         */
        public void setMaxRetryDelaySeconds(Long maxRetryDelaySeconds) {
            this.maxRetryDelaySeconds = maxRetryDelaySeconds;
        }

        /**
         * MaxAttempts defines the maximum number of attempts to perform.
         * If this value is greater than 0, and the number of attempts reaches this limit,
         * the logic will give up retrying even if the total retry time is still lower
         * than TotalTimeout.
         * @return maxAttempts
         */
        public Integer getMaxAttempts() {
            return this.maxAttempts;
        }

        /**
         * MaxAttempts defines the maximum number of attempts to perform.
         * If this value is greater than 0, and the number of attempts reaches this limit,
         * the logic will give up retrying even if the total retry time is still lower
         * than TotalTimeout.
         * @param maxAttempts
         */
        public void setMaxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        /**
         * Jitter determines if the delay time should be randomized.
         * @return jittered
         */
        public Boolean getJittered() {
            return this.jittered;
        }

        /**
         * Jitter determines if the delay time should be randomized.
         * @param jittered
         */
        public void setJittered(Boolean jittered) {
            this.jittered = jittered;
        }

        /**
         * InitialRpcTimeout controls the timeout for the initial RPC. Subsequent calls will use this
         * value adjusted according to the RpcTimeoutMultiplier.
         * @return initialRpcTimeoutSeconds
         */
        public Long getInitialRpcTimeoutSeconds() {
            return this.initialRpcTimeoutSeconds;
        }

        /**
         * InitialRpcTimeout controls the timeout for the initial RPC. Subsequent calls will use this
         * value adjusted according to the RpcTimeoutMultiplier.
         * @param initialRpcTimeoutSeconds
         */
        public void setInitialRpcTimeoutSeconds(Long initialRpcTimeoutSeconds) {
            this.initialRpcTimeoutSeconds = initialRpcTimeoutSeconds;
        }

        /**
         * RpcTimeoutMultiplier controls the change in RPC timeout. The timeout of the previous call is
         * multiplied by the RpcTimeoutMultiplier to calculate the timeout for the next call.
         * @return rpcTimeoutMultiplier
         */
        public Double getRpcTimeoutMultiplier() {
            return this.rpcTimeoutMultiplier;
        }

        /**
         * RpcTimeoutMultiplier controls the change in RPC timeout. The timeout of the previous call is
         * multiplied by the RpcTimeoutMultiplier to calculate the timeout for the next call.
         * @param rpcTimeoutMultiplier
         */
        public void setRpcTimeoutMultiplier(Double rpcTimeoutMultiplier) {
            this.rpcTimeoutMultiplier = rpcTimeoutMultiplier;
        }

        /**
         * get the max RPC timeout seconds.
         * @return maxRpcTimeoutSeconds
         */
        public Long getMaxRpcTimeoutSeconds() {
            return this.maxRpcTimeoutSeconds;
        }

        /**
         * get the max RPC timeout seconds.
         * @param maxRpcTimeoutSeconds
         */
        public void setMaxRpcTimeoutSeconds(Long maxRpcTimeoutSeconds) {
            this.maxRpcTimeoutSeconds = maxRpcTimeoutSeconds;
        }
    }


    /**
     * Batching settings.
     */
    @ConfigurationProperties("batching")
    public static class Batching {

        private final FlowControl flowControl = new FlowControl();

        private Long elementCountThreshold;

        private Long requestByteThreshold;

        private Long delayThresholdSeconds;

        private Boolean enabled;

        /**
         * The element count threshold to use for batching.
         * @return elementCountThreshold
         */
        public Long getElementCountThreshold() {
            return this.elementCountThreshold;
        }

        /**
         * The element count threshold to use for batching.
         * @param elementCountThreshold
         */

        public void setElementCountThreshold(Long elementCountThreshold) {
            this.elementCountThreshold = elementCountThreshold;
        }

        /**
         * The request byte threshold to use for batching.
         * @return requestByteThreshold
         */
        public Long getRequestByteThreshold() {
            return this.requestByteThreshold;
        }

        /**
         * The request byte threshold to use for batching.
         * @param requestByteThreshold
         */
        public void setRequestByteThreshold(Long requestByteThreshold) {
            this.requestByteThreshold = requestByteThreshold;
        }

        /**
         * The delay threshold to use for batching. After this amount of time has elapsed (counting
         * from the first element added), the elements will be wrapped up in a batch and sent.
         * @return delayThresholdSeconds
         */
        public Long getDelayThresholdSeconds() {
            return this.delayThresholdSeconds;
        }

        /**
         * The delay threshold to use for batching. After this amount of time has elapsed (counting
         * from the first element added), the elements will be wrapped up in a batch and sent.
         * @param delayThresholdSeconds
         */
        public void setDelayThresholdSeconds(Long delayThresholdSeconds) {
            this.delayThresholdSeconds = delayThresholdSeconds;
        }

        /**
         * Enables batching if true.
         * @return enabled
         */
        public Boolean getEnabled() {
            return this.enabled;
        }

        /**
         * Enables batching if true.
         * @param enabled
         */
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Flow control settings for batching.
         * @return flowControl
         */
        public FlowControl getFlowControl() {
            return this.flowControl;
        }
    }

    /**
     * flow control settings.
     */
    @ConfigurationProperties("flow-control")
    public static class FlowControl {

        private Long maxOutstandingElementCount;

        private Long maxOutstandingRequestBytes;

        private FlowController.LimitExceededBehavior limitExceededBehavior;

        /**
         * Maximum number of outstanding elements to keep in memory before enforcing flow control.
         * @return maxOutstandingElementCount
         */
        public Long getMaxOutstandingElementCount() {
            return this.maxOutstandingElementCount;
        }

        /**
         * Maximum number of outstanding elements to keep in memory before enforcing flow control.
         * @param maxOutstandingElementCount
         */
        public void setMaxOutstandingElementCount(Long maxOutstandingElementCount) {
            this.maxOutstandingElementCount = maxOutstandingElementCount;
        }

        /**
         * Maximum number of outstanding bytes to keep in memory before enforcing flow control.
         * @return maxOutstandingRequestBytes
         */
        public Long getMaxOutstandingRequestBytes() {
            return this.maxOutstandingRequestBytes;
        }

        /**
         * Maximum number of outstanding bytes to keep in memory before enforcing flow control.
         * @param maxOutstandingRequestBytes
         */
        public void setMaxOutstandingRequestBytes(Long maxOutstandingRequestBytes) {
            this.maxOutstandingRequestBytes = maxOutstandingRequestBytes;
        }

        /**
         * The behavior when the specified limits are exceeded.
         * @return limitExceededBehavior
         */
        public FlowController.LimitExceededBehavior getLimitExceededBehavior() {
            return this.limitExceededBehavior;
        }

        /**
         * The behavior when the specified limits are exceeded.
         * @param limitExceededBehavior
         */
        public void setLimitExceededBehavior(
                FlowController.LimitExceededBehavior limitExceededBehavior) {
            this.limitExceededBehavior = limitExceededBehavior;
        }
    }

}
