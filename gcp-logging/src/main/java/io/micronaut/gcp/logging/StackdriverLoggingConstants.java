package io.micronaut.gcp.logging;
/**
 * Constants for Stackdriver Trace.
 *
 * Original source at: https://github.com/spring-cloud/spring-cloud-gcp/blob/master/spring-cloud-gcp-logging/src/main/java/org/springframework/cloud/gcp/logging/StackdriverTraceConstants.java
 *
 * @author João André Martins
 * @author Chengyuan Zhao
 * @author Vinicius Carvalho
 *
 * @since 3.0.0
 */
public interface StackdriverLoggingConstants {

    /**
     * The JSON field name for the log level (severity).
     */
    String SEVERITY_ATTRIBUTE = "severity";

    /**
     * The JSON field name for the seconds of the timestamp.
     */
    String TIMESTAMP_SECONDS_ATTRIBUTE = "timestampSeconds";

    /**
     * The JSON field name for the nanos of the timestamp.
     */
    String TIMESTAMP_NANOS_ATTRIBUTE = "timestampNanos";

    /**
     * The JSON field name for the span-id.
     */
    String SPAN_ID_ATTRIBUTE = "logging.googleapis.com/spanId";

    /**
     * The JSON field name for the trace-id.
     */
    String TRACE_ID_ATTRIBUTE = "logging.googleapis.com/trace";

    /**
     * The name of the MDC parameter, Spring Sleuth is storing the trace id at.
     */
    String MDC_FIELD_TRACE_ID = "X-B3-TraceId";

    /**
     * The name of the MDC parameter, Spring Sleuth is storing the span id at.
     */
    String MDC_FIELD_SPAN_ID = "X-B3-SpanId";

    /**
     * The name of the MDC parameter, Spring Sleuth is storing the span export information at.
     */
    String MDC_FIELD_SPAN_EXPORT = "X-Span-Export";

    /**
     * The JSON field name for the service context.
     * @since 1.2
     */
    String SERVICE_CONTEXT_ATTRIBUTE = "serviceContext";

    /**
     * Composes the full trace name as expected by the Google Developers Console log viewer, to
     * enable trace correlation with log entries.
     * @param projectId the GCP project ID
     * @param traceId the trace ID
     * @return the trace name in the "projects/[GCP_PROJECT_ID]/trace/[TRACE_ID]" format
     */
    static String composeFullTraceName(String projectId, String traceId) {
        return "projects/" + projectId + "/traces/" + traceId;
    }

}
