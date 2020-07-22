package io.micronaut.gcp.pubsub.configuration;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.util.StringUtils;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.Duration;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class ThreeTenConverterRegistrar implements TypeConverterRegistrar {

    private static final Pattern DURATION_MATCHER = Pattern.compile("^(-?\\d+)([unsmhd])(s?)$");
    private static final int MILLIS = 3;

    @Override
    public void register(ConversionService<?> conversionService) {
        conversionService.addConverter(
                CharSequence.class,
                Duration.class,
                (object, targetType, context) -> {
                    String value = object.toString().trim();
                    if (value.startsWith("P")) {
                        try {
                            return Optional.of(Duration.parse(value));
                        } catch (DateTimeException e) {
                            context.reject(value, e);
                            return Optional.empty();
                        }
                    } else {
                        Matcher matcher = DURATION_MATCHER.matcher(value);
                        if (matcher.find()) {
                            String amount = matcher.group(1);
                            final String g2 = matcher.group(2);
                            char type = g2.charAt(0);
                            try {
                                switch (type) {
                                    case 's':
                                        return Optional.of(Duration.ofSeconds(Long.valueOf(amount)));
                                    case 'm':
                                        String ms = matcher.group(MILLIS);
                                        if (StringUtils.hasText(ms)) {
                                            return Optional.of(Duration.ofMillis(Long.valueOf(amount)));
                                        } else {
                                            return Optional.of(Duration.ofMinutes(Long.valueOf(amount)));
                                        }
                                    case 'h':
                                        return Optional.of(Duration.ofHours(Long.valueOf(amount)));
                                    case 'd':
                                        return Optional.of(Duration.ofDays(Long.valueOf(amount)));
                                    default:
                                        final String seq = g2 + matcher.group(3);
                                        switch (seq) {
                                            case "ns":
                                                return Optional.of(Duration.ofNanos(Long.valueOf(amount)));
                                            default:
                                                context.reject(
                                                        value,
                                                        new DateTimeException("Unparseable date format (" + value + "). Should either be a ISO-8601 duration or a round number followed by the unit type"));
                                                return Optional.empty();
                                        }
                                }
                            } catch (NumberFormatException e) {
                                context.reject(value, e);
                            }
                        }
                    }
                    return Optional.empty();
                }
        );
    }
}
