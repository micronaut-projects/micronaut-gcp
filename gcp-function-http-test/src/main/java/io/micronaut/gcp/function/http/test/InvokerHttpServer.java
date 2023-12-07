/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.gcp.function.http.test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.gcp.function.http.HttpFunction;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Singleton;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An embedded server implementation that uses the function invoker.
 *
 * Only for testing purposes.
 *
 * @author gkrocher
 * @since 3.0.2
 */
@Singleton
@Internal
@Experimental
public class InvokerHttpServer implements EmbeddedServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokerHttpServer.class);
    private final ApplicationContext applicationContext;
    private final ServerPort serverPort;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Server server;
    private HttpFunction httpFunction;

    public InvokerHttpServer(ApplicationContext applicationContext, HttpServerConfiguration httpServerConfiguration) {
        this.applicationContext = applicationContext;
        this.serverPort = createServerPort(httpServerConfiguration);
    }

    private ServerPort createServerPort(HttpServerConfiguration httpServerConfiguration) {
        Optional<Integer> portOpt = httpServerConfiguration.getPort();
        if (portOpt.isPresent()) {
            Integer port = portOpt.get();
            if (port == -1) {
                return new ServerPort(true, 0);

            } else {
                return new ServerPort(false, port);
            }
        } else {
            if (applicationContext.getEnvironment().getActiveNames().contains(Environment.TEST)) {
                return new ServerPort(true, 0);
            } else {
                return new ServerPort(false, 8080);
            }
        }
    }

    @Override
    public EmbeddedServer start() {
        if (running.compareAndSet(false, true)) {
            int port = serverPort.port();
            try {
                this.server = new Server(port);

                ServletContextHandler servletContextHandler = new ServletContextHandler();
                servletContextHandler.setContextPath("/");
                server.setHandler(NotFoundHandler.forServlet(servletContextHandler));

                httpFunction = new HttpFunction() {
                    @Override
                    protected ApplicationContext buildApplicationContext(@Nullable Object context) {
                        ApplicationContext ctx = InvokerHttpServer.this.getApplicationContext();
                        this.applicationContext = ctx;
                        return ctx;
                    }
                };
                ServletHolder servletHolder = getServletHolder();
                servletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(""));
                servletContextHandler.addServlet(servletHolder, "/*");

                this.server.start();
            } catch (Exception e) {
                throw new ServerStartupException(e.getMessage(), e);
            }
        }
        return this;
    }

    private ServletHolder getServletHolder() {
        HttpServlet servlet = new HttpServlet() {

            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
                try {
                    httpFunction.service(
                        new HttpRequestImpl(req),
                        new HttpResponseImpl(resp)
                    );
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
        };
        return new ServletHolder(servlet);
    }

    /**
     * The function class.
     *
     * @return The function class
     */
    protected Class<?> getFunctionClass() {
        return HttpFunction.class;
    }

    @Override
    public EmbeddedServer stop() {
        if (running.compareAndSet(true, false)) {
            try {
                httpFunction.close();
                applicationContext.close();
                server.stop();
            } catch (Exception e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Error shutting down Cloud Function server: " + e.getMessage(), e);
                }
            }
            server = null;
        }
        return this;
    }

    @Override
    public int getPort() {
        return server.getURI().getPort();
    }

    @Override
    public String getHost() {
        return "localhost";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public URL getURL() {
        String spec = getScheme() + "://" + getHost() + ":" + getPort();
        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            throw new HttpServerException("Invalid server URL " + spec);
        }
    }

    @Override
    public URI getURI() {
        try {
            return getURL().toURI();
        } catch (URISyntaxException e) {
            throw new HttpServerException("Invalid server URL " + getURL());
        }
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationContext.getBean(ApplicationConfiguration.class);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Wrapper that intercepts requests for {@code /favicon.ico} and {@code /robots.txt} and causes
     * them to produce a 404 status. Otherwise they would be sent to the function code, like any
     * other URL, meaning that someone testing their function by using a browser as an HTTP client
     * can see two requests, one for {@code /favicon.ico} and one for {@code /} (or whatever).
     */
    private static class NotFoundHandler extends HandlerWrapper {

        private static final Set<String> NOT_FOUND_PATHS = new HashSet<>(Arrays.asList("/favicon.ico", "/robots.txt"));

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {
            if (NOT_FOUND_PATHS.contains(request.getRequestURI())) {
                response.sendError(HttpStatus.NOT_FOUND_404, "Not Found");
            } else {
                super.handle(target, baseRequest, request, response);
            }
        }

        static NotFoundHandler forServlet(ServletContextHandler servletHandler) {
            NotFoundHandler handler = new NotFoundHandler();
            handler.setHandler(servletHandler);
            return handler;
        }
    }

    private record ServerPort(boolean random, Integer port) {
    }
}
