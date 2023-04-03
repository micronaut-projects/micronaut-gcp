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
package io.micronaut.gcp.function.http.test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.gcp.function.http.HttpFunction;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
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

import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.*;
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
    private final HttpServerConfiguration serverConfiguration;
    private final boolean randomPort;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private int port;
    private Server server;

    public InvokerHttpServer(ApplicationContext applicationContext, HttpServerConfiguration serverConfiguration) {
        this.applicationContext = applicationContext;
        this.serverConfiguration = serverConfiguration;
        Optional<Integer> port = serverConfiguration.getPort();
        if (port.isPresent()) {
            this.port = port.get();
            if (this.port == -1) {
                this.port = SocketUtils.findAvailableTcpPort();
                this.randomPort = true;
            } else {
                this.randomPort = false;
            }
        } else {
            if (applicationContext.getEnvironment().getActiveNames().contains(Environment.TEST)) {
                this.randomPort = true;
                this.port = SocketUtils.findAvailableTcpPort();
            } else {
                this.randomPort = false;
                this.port = 8080;
            }
        }
    }

    @Override
    public EmbeddedServer start() {
        if (running.compareAndSet(false, true)) {
            int retryCount = 0;
            while (retryCount <= 3) {
                try {
                    this.server = new Server(port);

                    ServletContextHandler servletContextHandler = new ServletContextHandler();
                    servletContextHandler.setContextPath("/");
                    server.setHandler(NotFoundHandler.forServlet(servletContextHandler));

                    HttpFunction httpFunction = new HttpFunction() {
                        @Override
                        protected ApplicationContext buildApplicationContext(@Nullable Object context) {
                            ApplicationContext ctx = InvokerHttpServer.this.getApplicationContext();
                            this.applicationContext = ctx;
                            return ctx;
                        }
                    };
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
                    ServletHolder servletHolder = new ServletHolder(servlet);
                    servletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(""));
                    servletContextHandler.addServlet(servletHolder, "/*");

                    server.start();
                    logServerInfo();
                    break;
                } catch (BindException e) {
                    if (randomPort) {
                        this.port = SocketUtils.findAvailableTcpPort();
                        retryCount++;
                    } else {
                        throw new ServerStartupException(e.getMessage(), e);
                    }
                } catch (Exception e) {
                    throw new ServerStartupException(
                            "Error starting Google Cloud Function server: " + e.getMessage(),
                            e
                    );
                }
            }

        }
        return this;
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
        return port;
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
        return serverConfiguration.getApplicationConfiguration();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void logServerInfo() {
        LOGGER.info("Serving function...");
        LOGGER.info("Function: {}", getFunctionClass().getName());
        LOGGER.info("URL: http://localhost:{}/", port);
    }

    /**
     * Wrapper that intercepts requests for {@code /favicon.ico} and {@code /robots.txt} and causes
     * them to produce a 404 status. Otherwise they would be sent to the function code, like any
     * other URL, meaning that someone testing their function by using a browser as an HTTP client
     * can see two requests, one for {@code /favicon.ico} and one for {@code /} (or whatever).
     */
    private static class NotFoundHandler extends HandlerWrapper {
        private static final Set<String> NOT_FOUND_PATHS =
                new HashSet<>(Arrays.asList("/favicon.ico", "/robots.txt"));

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
}
