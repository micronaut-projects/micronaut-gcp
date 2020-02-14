package io.micronaut.gcp.function.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.util.ArgumentUtils;

import javax.annotation.Nullable;
import java.util.*;

@Internal
class GoogleMultiValueMap implements ConvertibleMultiValues<String> {
    final Map<String, List<String>> map;

    GoogleMultiValueMap(Map<String, List<String>> map) {
        this.map = map;
    }

    @Override
    public List<String> getAll(CharSequence name) {
        ArgumentUtils.requireNonNull("name", name);
        return map.getOrDefault(name, Collections.emptyList());
    }

    @Nullable
    @Override
    public String get(CharSequence name) {
        ArgumentUtils.requireNonNull("name", name);

        final List<String> values = map.get(name.toString());
        final Iterator<String> i = values.iterator();
        if (i.hasNext()) {
            return i.next();
        }
        return null;
    }

    @Override
    public Set<String> names() {
        return map.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        return map.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        final String v = get(name);
        if (v != null) {
            return ConversionService.SHARED.convert(v, conversionContext);
        }
        return Optional.empty();
    }
}
