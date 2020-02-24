package io.micronaut.function.http;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.*;

/**
 * Implementation of {@link Cookies} for serverless.
 *
 * @author graemerocher
 * @since 2.0
 */
public class ServerlessCookies implements Cookies {

    private final ConversionService<?> conversionService;
    private final Map<CharSequence, Cookie> cookies;

    /**
     * @param path              The path
     * @param headers      The Netty HTTP headers
     * @param conversionService The conversion service
     */
    public ServerlessCookies(String path, HttpHeaders headers, ConversionService conversionService) {
        this.conversionService = conversionService;
        String value = headers.get(HttpHeaders.COOKIE);
        if (value != null) {
            cookies = new LinkedHashMap<>(10);
            Set<io.netty.handler.codec.http.cookie.Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(value);
            for (io.netty.handler.codec.http.cookie.Cookie nettyCookie : nettyCookies) {
                String cookiePath = nettyCookie.path();
                if (cookiePath != null) {
                    if (path.startsWith(cookiePath)) {
                        cookies.put(nettyCookie.name(), new NettyCookie(nettyCookie));
                    }
                } else {
                    cookies.put(nettyCookie.name(), new NettyCookie(nettyCookie));
                }
            }
        } else {
            cookies = Collections.emptyMap();
        }
    }

    @Override
    public Set<Cookie> getAll() {
        return new HashSet<>(cookies.values());
    }

    @Override
    public Optional<Cookie> findCookie(CharSequence name) {
        Cookie cookie = cookies.get(name);
        return cookie != null ? Optional.of(cookie) : Optional.empty();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, Class<T> requiredType) {
        if (requiredType == Cookie.class || requiredType == Object.class) {
            //noinspection unchecked
            return (Optional<T>) findCookie(name);
        } else {
            return findCookie(name).flatMap((cookie -> conversionService.convert(cookie.getValue(), requiredType)));
        }
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        return findCookie(name).flatMap((cookie -> conversionService.convert(cookie.getValue(), conversionContext)));
    }

    @Override
    public Collection<Cookie> values() {
        return Collections.unmodifiableCollection(cookies.values());
    }
}
