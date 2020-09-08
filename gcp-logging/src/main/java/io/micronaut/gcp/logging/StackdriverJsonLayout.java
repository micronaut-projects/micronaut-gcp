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
package io.micronaut.gcp.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.ServiceOptions;
import io.micronaut.core.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StackdriverJsonLayout extends JsonLayout {

    private static final Set<String> FILTERED_MDC_FIELDS = new HashSet<>(Arrays.asList(
            StackdriverLoggingConstants.MDC_FIELD_TRACE_ID,
            StackdriverLoggingConstants.MDC_FIELD_SPAN_ID,
            StackdriverLoggingConstants.MDC_FIELD_SPAN_EXPORT));

    private String projectId;

    private boolean includeTraceId;

    private boolean includeSpanId;

    private boolean includeExceptionInMessage;

    private Map<String, Object> customJson;

    public StackdriverJsonLayout() {
        this.appendLineSeparator = true;
        this.includeExceptionInMessage = true;
        this.includeException = false;
        this.includeTraceId = true;
        this.includeSpanId = true;
        ObjectMapper mapper = new ObjectMapper();
        setJsonFormatter(mapper::writeValueAsString);
    }

    @Override
    public void start() {
        super.start();
        // If no Project ID set, then attempt to resolve it with the default project ID provider
        if (StringUtils.isEmpty(this.projectId)) {
            this.projectId = ServiceOptions.getDefaultProjectId();
        }
    }

    /**
     * Convert a logging event into a Map.
     * @param event the logging event
     * @return the map which should get rendered as JSON
     */
    @Override
    protected Map<String, Object> toJsonMap(ILoggingEvent event) {

        Map<String, Object> map = new LinkedHashMap<>();

        if (this.includeMDC) {
            event.getMDCPropertyMap().forEach((key, value) -> {
                if (!FILTERED_MDC_FIELDS.contains(key)) {
                    map.put(key, value);
                }
            });
        }
        if (this.includeTimestamp) {
            map.put(StackdriverLoggingConstants.TIMESTAMP_SECONDS_ATTRIBUTE,
                    TimeUnit.MILLISECONDS.toSeconds(event.getTimeStamp()));
            map.put(StackdriverLoggingConstants.TIMESTAMP_NANOS_ATTRIBUTE,
                    TimeUnit.MILLISECONDS.toNanos(event.getTimeStamp() % 1_000));
        }

        add(StackdriverLoggingConstants.SEVERITY_ATTRIBUTE, this.includeLevel,
                String.valueOf(event.getLevel()), map);
        add(JsonLayout.THREAD_ATTR_NAME, this.includeThreadName, event.getThreadName(), map);
        add(JsonLayout.LOGGER_ATTR_NAME, this.includeLoggerName, event.getLoggerName(), map);

        if (this.includeFormattedMessage) {
            String message = event.getFormattedMessage();
            if (this.includeExceptionInMessage) {
                IThrowableProxy throwableProxy = event.getThrowableProxy();
                if (throwableProxy != null) {
                    String stackTrace = getThrowableProxyConverter().convert(event);
                    if (stackTrace != null && !stackTrace.equals("")) {
                        message += "\n" + stackTrace;
                    }
                }
            }
            map.put(JsonLayout.FORMATTED_MESSAGE_ATTR_NAME, message);
        }
        add(JsonLayout.MESSAGE_ATTR_NAME, this.includeMessage, event.getMessage(), map);
        add(JsonLayout.CONTEXT_ATTR_NAME, this.includeContextName, event.getLoggerContextVO().getName(), map);
        addThrowableInfo(JsonLayout.EXCEPTION_ATTR_NAME, this.includeException, event, map);
        addTraceId(event, map);
        add(StackdriverLoggingConstants.SPAN_ID_ATTRIBUTE, this.includeSpanId,
                event.getMDCPropertyMap().get(StackdriverLoggingConstants.MDC_FIELD_SPAN_ID), map);
//        if (this.serviceContext != null) {
//            map.put(StackdriverLoggingConstants.SERVICE_CONTEXT_ATTRIBUTE, this.serviceContext);
//        }
        if (this.customJson != null && !this.customJson.isEmpty()) {
            for (Map.Entry<String, Object> entry : this.customJson.entrySet()) {
                map.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        addCustomDataToJsonMap(map, event);
        return map;
    }


    private void addTraceId(ILoggingEvent event, Map<String, Object> map) {
        if (!this.includeTraceId) {
            return;
        }

        String traceId =
                event.getMDCPropertyMap().get(StackdriverLoggingConstants.MDC_FIELD_TRACE_ID);
//        if (traceId == null) {
//            traceId = TraceIdLoggingEnhancer.getCurrentTraceId();
//        }
        if (!StringUtils.isEmpty(traceId)
                && !StringUtils.isEmpty(this.projectId)
                && !this.projectId.endsWith("_IS_UNDEFINED")) {
            traceId = StackdriverLoggingConstants.composeFullTraceName(
                    this.projectId, String.format("%016d", traceId));
        }

        add(StackdriverLoggingConstants.TRACE_ID_ATTRIBUTE, this.includeTraceId, traceId, map);
    }
}
