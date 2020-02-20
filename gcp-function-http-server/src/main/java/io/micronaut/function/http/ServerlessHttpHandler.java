package io.micronaut.function.http;


import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRouteMatch;
import io.micronaut.web.router.exceptions.DuplicateRouteException;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An HTTP handler that can deal with Serverless requests.
 *
 * @param <Req> The request object
 * @param <Res> The response object
 * @author graemerocher
 * @since 1.2.0
 */
public abstract class ServerlessHttpHandler<Req, Res> extends FunctionInitializer implements AutoCloseable {
    /**
     * Logger to be used by subclasses for logging.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(ServerlessHttpHandler.class);
    private final Router router;
    private final RequestArgumentSatisfier requestArgumentSatisfier;
    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;

    public ServerlessHttpHandler() {
        this.router = applicationContext.getBean(Router.class);
        this.requestArgumentSatisfier = applicationContext.getBean(RequestArgumentSatisfier.class);
        this.mediaTypeCodecRegistry = applicationContext.getBean(MediaTypeCodecRegistry.class);
    }

    /**
     * @return The router
     */
    public Router getRouter() {
        return router;
    }

    /**
     * @return The request argument satisfier
     */
    public RequestArgumentSatisfier getRequestArgumentSatisfier() {
        return requestArgumentSatisfier;
    }

    /**
     * @return The media type codec registry
     */
    public MediaTypeCodecRegistry getMediaTypeCodecRegistry() {
        return mediaTypeCodecRegistry;
    }

    /**
     * Handle the give native request and response.
     * @param request The request
     * @param response The response
     */
    public void service(Req request, Res response) {
        try {

            ServerlessExchange exchange = createExchange(request, response);
            service(exchange);
        } finally {
            applicationContext.close();
        }
    }

    /**
     * Handle the given Micronaut request and native response.
     * @param request The request
     * @param response The response
     */
    public void service(HttpRequest<? super Object> request, Res response) {
        try {
            ServerlessExchange exchange = createExchange(request, response);
            service(exchange);
        } finally {
            applicationContext.close();
        }
    }

    /**
     * Handles a {@link ServerlessExchange}.
     * @param exchange The exchange
     */
    public void service(ServerlessExchange exchange) {
        final long time = System.currentTimeMillis();
        try {
            final MutableHttpResponse<Object> res = exchange.getResponse();
            final HttpRequest<Object> req = exchange.getRequest();
            ServerRequestContext.with(req, () -> {
                final List<UriRouteMatch<Object, Object>> matchingRoutes = router.findAllClosest(req);
                if (CollectionUtils.isNotEmpty(matchingRoutes)) {
                    RouteMatch<Object> route;
                    if (matchingRoutes.size() > 1) {
                        throw new DuplicateRouteException(req.getPath(), matchingRoutes);
                    } else {
                        UriRouteMatch<Object, Object> establishedRoute = matchingRoutes.get(0);
                        req.setAttribute(HttpAttributes.ROUTE, establishedRoute.getRoute());
                        req.setAttribute(HttpAttributes.ROUTE_MATCH, establishedRoute);
                        req.setAttribute(HttpAttributes.URI_TEMPLATE, establishedRoute.getRoute().getUriMatchTemplate().toString());
                        route = establishedRoute;
                    }


                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Matched route {} - {} to controller {}", req.getMethodName(), req.getPath(), route.getDeclaringType());
                    }

                    invokeRouteMatch(req, res, route, false);

                } else {
                    final RouteMatch<Object> notFoundRoute =
                            router.route(HttpStatus.NOT_FOUND).orElse(null);

                    if (notFoundRoute != null) {
                        invokeRouteMatch(req, res, notFoundRoute, true);
                    } else {
                        res.status(HttpStatus.NOT_FOUND);
                    }
                }
            });
        } finally {
            if (LOG.isInfoEnabled()) {
                final HttpRequest<? super Object> r = exchange.getRequest();
                LOG.info("Executed HTTP Function [{} {}] in: {}ms",
                        r.getMethod(),
                        r.getPath(),
                        (System.currentTimeMillis() - time)
                );
            }
        }
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

    @Override
    public void close() throws IOException {
        if (applicationContext.isRunning()) {
            applicationContext.close();
        }
    }

    private void invokeRouteMatch(
            HttpRequest<Object> req,
            MutableHttpResponse<Object> res,
            RouteMatch<?> route,
            boolean isErrorRoute) {
        if (!route.isExecutable()) {
            route = requestArgumentSatisfier.fulfillArgumentRequirements(route, req, false);
        }
        RouteMatch<?> finalRoute = route;
        final AnnotationMetadata annotationMetadata = finalRoute.getAnnotationMetadata();
        Publisher<? extends MutableHttpResponse<?>> responsePublisher
                = Flowable.defer(() -> {
            annotationMetadata.stringValue(Produces.class)
                    .ifPresent(res::contentType);
            annotationMetadata.enumValue(Status.class, HttpStatus.class)
                    .ifPresent(s -> res.status(s));
            final Object result = finalRoute.execute();
            if (result == null) {
                return Publishers.just(res);
            }
            if (Publishers.isConvertibleToPublisher(result)) {
                final Publisher<?> publisher = Publishers.convertPublisher(result, Publisher.class);
                return Publishers.map(publisher, o -> {
                    if (o instanceof MutableHttpResponse) {
                        return (MutableHttpResponse<?>) o;
                    } else {
                        res.body(o);
                        return res;
                    }
                });
            } else if (result instanceof MutableHttpResponse) {
                return Publishers.just((MutableHttpResponse<?>) result);
            } else {
                return Publishers.just(
                        res.body(result)
                );
            }
        });
        final List<HttpFilter> filters = router.findFilters(req);
        if (CollectionUtils.isNotEmpty(filters)) {
            responsePublisher =
                    filterPublisher(new AtomicReference<>(req), responsePublisher, isErrorRoute);
        }

        try {
            Flowable.fromPublisher(responsePublisher)
                    .blockingSubscribe();
        } catch (Throwable e) {
            if (isErrorRoute) {
                // handle error default
                if (LOG.isErrorEnabled()) {
                    LOG.error("Error occurred executing Error route [" + route + "]: " + e.getMessage(), e);
                }
                res.status(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            } else {
                if (e instanceof UnsatisfiedRouteException) {
                    final RouteMatch<Object> badRequestRoute = lookupStatusRoute(route, HttpStatus.BAD_REQUEST);
                    if (badRequestRoute != null) {
                        invokeRouteMatch(req, res, badRequestRoute, true);
                    } else {
                        res.status(HttpStatus.BAD_REQUEST, e.getMessage());
                    }
                } else {

                    final RouteMatch<Object> errorRoute = lookupErrorRoute(route, e);
                    if (errorRoute != null) {
                        invokeRouteMatch(req, res, errorRoute, true);
                    } else {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Error occurred executing Error route [" + route + "]: " + e.getMessage(), e);
                        }
                        res.status(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Creates the {@link ServerlessExchange} object.
     *
     * @param request  The request
     * @param response The response
     * @return The exchange object
     */
    protected abstract ServerlessExchange createExchange(Req request, Res response);

    /**
     * Creates the {@link ServerlessExchange} object.
     *
     * @param request  The request
     * @param response The response
     * @return The exchange object
     */
    protected abstract ServerlessExchange createExchange(HttpRequest<? super Object> request, Res response);

    private RouteMatch<Object> lookupErrorRoute(RouteMatch<?> route, Throwable e) {
        return router.route(route.getDeclaringType(), e)
                .orElseGet(() -> router.route(e).orElse(null));
    }

    private RouteMatch<Object> lookupStatusRoute(RouteMatch<?> route, HttpStatus status) {
        return router.route(route.getDeclaringType(), status)
                .orElseGet(() ->
                        router.route(status).orElse(null)
                );
    }

    private Publisher<? extends MutableHttpResponse<?>> filterPublisher(
            AtomicReference<io.micronaut.http.HttpRequest<?>> requestReference,
            Publisher<? extends MutableHttpResponse<?>> routePublisher,
            boolean skipOncePerRequest) {
        Publisher<? extends io.micronaut.http.MutableHttpResponse<?>> finalPublisher;
        List<HttpFilter> filters = new ArrayList<>(router.findFilters(requestReference.get()));
        if (skipOncePerRequest) {
            filters.removeIf(filter -> filter instanceof OncePerRequestHttpServerFilter);
        }
        if (!filters.isEmpty()) {
            // make the action executor the last filter in the chain
            filters.add((HttpServerFilter) (req, chain) -> (Publisher<MutableHttpResponse<?>>) routePublisher);

            AtomicInteger integer = new AtomicInteger();
            int len = filters.size();
            ServerFilterChain filterChain = new ServerFilterChain() {
                @SuppressWarnings("unchecked")
                @Override
                public Publisher<MutableHttpResponse<?>> proceed(io.micronaut.http.HttpRequest<?> request) {
                    int pos = integer.incrementAndGet();
                    if (pos > len) {
                        throw new IllegalStateException("The FilterChain.proceed(..) method should be invoked exactly once per filter execution. The method has instead been invoked multiple times by an erroneous filter definition.");
                    }
                    HttpFilter httpFilter = filters.get(pos);
                    return (Publisher<MutableHttpResponse<?>>) httpFilter.doFilter(requestReference.getAndSet(request), this);
                }
            };
            HttpFilter httpFilter = filters.get(0);
            Publisher<? extends io.micronaut.http.HttpResponse<?>> resultingPublisher = httpFilter.doFilter(requestReference.get(), filterChain);
            finalPublisher = (Publisher<? extends MutableHttpResponse<?>>) resultingPublisher;
        } else {
            finalPublisher = routePublisher;
        }
        return finalPublisher;
    }
}
