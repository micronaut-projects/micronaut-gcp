package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.servlet.http.DefaultServletExchange;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import io.netty.util.internal.MacAddressUtil;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Entry point into the Micronaut + GCP integration.
 *
 * @author graemerocher
 * @since 1.2.0
 */
public class HttpFunction extends FunctionInitializer implements com.google.cloud.functions.HttpFunction {

    static {
        byte[] bestMacAddr = new byte[8];
        PlatformDependent.threadLocalRandom().nextBytes(bestMacAddr);
        System.setProperty("io.netty.machineId", MacAddressUtil.formatAddress(bestMacAddr));
    }

    protected static final Logger LOG = LoggerFactory.getLogger(HttpFunction.class);

    private final ServletHttpHandler<HttpRequest, HttpResponse> httpHandler;

    /**
     * Default constructor.
     */
    public HttpFunction() {
        this.httpHandler = new ServletHttpHandler<HttpRequest, HttpResponse>(applicationContext) {
            @Override
            protected ServletExchange<HttpRequest, HttpResponse> createExchange(HttpRequest request, HttpResponse response) {
                final GoogleFunctionHttpResponse<Object> res =
                        new GoogleFunctionHttpResponse<>(response, getMediaTypeCodecRegistry());
                final GoogleFunctionHttpRequest<Object> req =
                        new GoogleFunctionHttpRequest<>(request, res, getMediaTypeCodecRegistry());

                return new DefaultServletExchange<>(req, res);
            }

            @Override
            public void service(HttpRequest request, HttpResponse response) {
                ServletExchange<HttpRequest, HttpResponse> exchange = createExchange(request, response);
                service(exchange);
            }
        };

        Runtime.getRuntime().addShutdownHook(
                new Thread(httpHandler::close)
        );
    }

    @Override
    protected void startThis(ApplicationContext applicationContext) {
        final long time = System.currentTimeMillis();
        try {
            super.startThis(applicationContext);
        } finally {
            if (LOG.isInfoEnabled()) {
                LOG.info("Initialized function in: " + (System.currentTimeMillis() - time) + "ms");
            }
        }
    }

    @Nonnull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        final ApplicationContextBuilder builder = super.newApplicationContextBuilder();
        builder.deduceEnvironment(false);
        builder.environments(Environment.FUNCTION, Environment.GOOGLE_COMPUTE);
        return builder;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        httpHandler.service(request, response);
    }
}
