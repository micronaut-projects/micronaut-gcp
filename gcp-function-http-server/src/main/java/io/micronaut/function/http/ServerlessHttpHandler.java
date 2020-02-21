package io.micronaut.function.http;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRoute;
import io.micronaut.web.router.UriRouteMatch;
import io.micronaut.web.router.exceptions.DuplicateRouteException;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * An HTTP handler that can deal with Serverless requests.
 *
 * @param <Req> The request object
 * @param <Res> The response object
 * @author graemerocher
 * @since 1.2.0
 */
public abstract class ServerlessHttpHandler<Req, Res> implements AutoCloseable {
    /**
     * Logger to be used by subclasses for logging.
     */
    static final Logger LOG = LoggerFactory.getLogger(ServerlessHttpHandler.class);

    private final Router router;
    private final RequestArgumentSatisfier requestArgumentSatisfier;
    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;
    private final ApplicationContext applicationContext;

    /**
     * Default constructor.
     *
     * @param applicationContext The application context
     */
    public ServerlessHttpHandler(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "The application context cannot be null");
        this.router = applicationContext.getBean(Router.class);
        this.requestArgumentSatisfier = applicationContext.getBean(RequestArgumentSatisfier.class);
        this.mediaTypeCodecRegistry = applicationContext.getBean(MediaTypeCodecRegistry.class);
    }

    /**
     * @return The application context for the function.
     */
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    /**
     * @return The media type codec registry.
     */
    public MediaTypeCodecRegistry getMediaTypeCodecRegistry() {
        return mediaTypeCodecRegistry;
    }

    /**
     * Handle the give native request and response.
     *
     * @param request  The request
     * @param response The response
     */
    public void service(Req request, Res response) {
        try {

            ServerlessExchange<Req, Res> exchange = createExchange(request, response);
            service(exchange);
        } finally {
            applicationContext.close();
        }
    }

    /**
     * Handles a {@link DefaultServerlessExchange}.
     *
     * @param exchange The exchange
     */
    public void service(ServerlessExchange<Req, Res> exchange) {
        final long time = System.currentTimeMillis();
        try {
            final MutableHttpResponse<Object> res = exchange.getResponse();
            final HttpRequest<Object> req = exchange.getRequest();
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
                Set<String> existingRouteMethods = router
                        .findAny(req.getUri().toString(), req)
                        .map(UriRouteMatch::getRoute)
                        .map(UriRoute::getHttpMethodName)
                        .collect(Collectors.toSet());

                if (CollectionUtils.isNotEmpty(existingRouteMethods)) {
                    final RouteMatch<Object> notAllowedRoute =
                            router.route(HttpStatus.METHOD_NOT_ALLOWED).orElse(null);

                    if (notAllowedRoute != null) {
                        invokeRouteMatch(req, res, notAllowedRoute, true);
                    } else {
                        res.getHeaders().allowGeneric(existingRouteMethods);
                        res.status(HttpStatus.METHOD_NOT_ALLOWED);
                    }
                } else {
                    final RouteMatch<Object> notFoundRoute =
                            router.route(HttpStatus.NOT_FOUND).orElse(null);

                    if (notFoundRoute != null) {
                        invokeRouteMatch(req, res, notFoundRoute, true);
                    } else {
                        res.status(HttpStatus.NOT_FOUND);
                    }
                }


            }
        } finally {
            if (LOG.isTraceEnabled()) {
                final HttpRequest<? super Object> r = exchange.getRequest();
                LOG.trace("Executed HTTP Request [{} {}] in: {}ms",
                        r.getMethod(),
                        r.getPath(),
                        (System.currentTimeMillis() - time)
                );
            }
        }
    }

    @Override
    public void close() {
        if (applicationContext.isRunning()) {
            applicationContext.close();
        }
    }

    private void invokeRouteMatch(
            HttpRequest<Object> req,
            MutableHttpResponse<Object> res,
            RouteMatch<?> route,
            boolean isErrorRoute) {

        try {
            if (!route.isExecutable()) {
                route = requestArgumentSatisfier.fulfillArgumentRequirements(route, req, false);
            }
            if (!route.isExecutable() && HttpMethod.permitsRequestBody(req.getMethod()) && !route.getBodyArgument().isPresent()) {
                final ConvertibleValues<?> convertibleValues = req.getBody(ConvertibleValues.class).orElse(null);
                if (convertibleValues != null) {

                    final Collection<Argument> requiredArguments = route.getRequiredArguments();
                    Map<String, Object> newValues = new HashMap<>(requiredArguments.size());
                    for (Argument requiredArgument : requiredArguments) {
                        final String name = requiredArgument.getName();
                        final Object v = convertibleValues.get(name, requiredArgument).orElse(null);
                        if (v != null) {
                            newValues.put(name, v);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(newValues)) {
                        route = route.fulfill(
                                newValues
                        );
                    }
                }
            }
            RouteMatch<?> finalRoute = route;
            final AnnotationMetadata annotationMetadata = finalRoute.getAnnotationMetadata();
            Publisher<? extends MutableHttpResponse<?>> responsePublisher
                    = Flowable.defer(() -> {
                annotationMetadata.stringValue(Produces.class)
                        .ifPresent(res::contentType);
                annotationMetadata.enumValue(Status.class, HttpStatus.class)
                        .ifPresent(s -> res.status(s));
                final List<AnnotationValue<Header>> headers = annotationMetadata.getAnnotationValuesByType(Header.class);
                for (AnnotationValue<Header> header : headers) {
                    final String value = header.stringValue().orElse(null);
                    final String name = header.stringValue("name").orElse(null);
                    if (name != null && value != null) {
                        res.header(name, value);
                    }
                }
                final Object result = finalRoute.execute();
                if (result == null) {
                    return Publishers.just(res);
                }
                if (Publishers.isConvertibleToPublisher(result)) {
                    final Publisher<?> publisher;
                    if (!Publishers.isSingle(result.getClass())) {
                        final Flowable flowable = Publishers.convertPublisher(result, Flowable.class);
                        publisher = Publishers.convertPublisher(flowable.toList(), Publisher.class);
                    } else {
                        publisher = Publishers.convertPublisher(result, Publisher.class);
                    }
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
            Flowable.fromPublisher(responsePublisher)
                    .blockingSubscribe(response -> {
                        final HttpStatus status = response.status();
                        if (!isErrorRoute && status.getCode() >= 400) {
                            final RouteMatch<Object> errorRoute = lookupStatusRoute(finalRoute, status);
                            if (errorRoute != null) {
                                invokeRouteMatch(req, res, errorRoute, true);
                            }
                        }
                    }, error -> handleException(req, res, finalRoute, isErrorRoute, error));
        } catch (Throwable e) {
            handleException(req, res, route, isErrorRoute, e);
        }
    }

    private void handleException(HttpRequest<Object> req, MutableHttpResponse<Object> res, RouteMatch<?> route, boolean isErrorRoute, Throwable e) {
        req.setAttribute(HttpAttributes.ERROR, e);
        if (isErrorRoute) {
            // handle error default
            if (LOG.isErrorEnabled()) {
                LOG.error("Error occurred executing Error route [" + route + "]: " + e.getMessage(), e);
            }
            res.status(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } else {
            if (e instanceof UnsatisfiedRouteException || e instanceof ConversionErrorException) {
                final RouteMatch<Object> badRequestRoute = lookupStatusRoute(route, HttpStatus.BAD_REQUEST);
                if (badRequestRoute != null) {
                    invokeRouteMatch(req, res, badRequestRoute, true);
                } else {
                    invokeExceptionHandlerIfPossible(req, res, e, HttpStatus.BAD_REQUEST);
                }
            } else if (e instanceof HttpStatusException) {
                HttpStatusException statusException = (HttpStatusException) e;
                final HttpStatus status = statusException.getStatus();
                final RouteMatch<Object> statusRoute = status.getCode() >= 400 ? lookupStatusRoute(route, status) : null;
                if (statusRoute != null) {
                    invokeRouteMatch(req, res, statusRoute, true);
                } else {
                    res.status(status.getCode(), statusException.getMessage());
                    statusException.getBody().ifPresent(res::body);
                }

            } else {

                final RouteMatch<Object> errorRoute = lookupErrorRoute(route, e);
                if (errorRoute != null) {
                    invokeRouteMatch(req, res, errorRoute, true);
                } else {
                    invokeExceptionHandlerIfPossible(req, res, e, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
    }

    private void invokeExceptionHandlerIfPossible(HttpRequest<Object> req, MutableHttpResponse<Object> res, Throwable e, HttpStatus defaultStatus) {
        final ExceptionHandler<Throwable, ?> exceptionHandler = lookupExceptionHandler(e);
        if (exceptionHandler != null) {
            try {
                ServerRequestContext.with(req, () -> {
                    exceptionHandler.handle(req, e);
                });
            } catch (Throwable ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Error occurred executing exception handler [" + exceptionHandler.getClass() + "]: " + e.getMessage(), e);
                }
                res.status(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            res.status(defaultStatus, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ExceptionHandler<Throwable, ?> lookupExceptionHandler(Throwable e) {
        final Class<? extends Throwable> type = e.getClass();
        return applicationContext.findBean(ExceptionHandler.class, Qualifiers.byTypeArgumentsClosest(type, HttpResponse.class))
                .orElse(null);
    }

    /**
     * Creates the {@link DefaultServerlessExchange} object.
     *
     * @param request  The request
     * @param response The response
     * @return The exchange object
     */
    protected abstract ServerlessExchange<Req, Res> createExchange(Req request, Res response);

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
            //noinspection unchecked
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
            //noinspection unchecked
            finalPublisher = (Publisher<? extends MutableHttpResponse<?>>) resultingPublisher;
        } else {
            finalPublisher = routePublisher;
        }
        return finalPublisher;
    }
}
