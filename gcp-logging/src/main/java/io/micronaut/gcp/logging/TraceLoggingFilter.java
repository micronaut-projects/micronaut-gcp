package io.micronaut.gcp.logging;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.FilterChain;
import io.micronaut.http.filter.HttpFilter;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Filter("/**")
@Requires(beans = Tracer.class)
public class TraceLoggingFilter implements HttpFilter {

    private final Tracer tracer;

    private final Logger logger = LoggerFactory.getLogger(TraceLoggingFilter.class);

    public TraceLoggingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(HttpRequest<?> request, FilterChain chain) {
        logger.info("My order is " + getOrder());
        logger.info("My tracer " + tracer.getClass().getName());
        logger.info("Current trace configuration");
        Span active = tracer.activeSpan();
        if (active != null) {
            SpanContext context = active.context();
            if (context != null) {
                logger.info("Span id: " + tracer.activeSpan().context().toSpanId());
                logger.info("Trace id: " + tracer.activeSpan().context().toTraceId());
            } else {
                logger.info("No active context");
            }
        } else {
            logger.info("No active span found");
        }
        return chain.proceed(request);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
