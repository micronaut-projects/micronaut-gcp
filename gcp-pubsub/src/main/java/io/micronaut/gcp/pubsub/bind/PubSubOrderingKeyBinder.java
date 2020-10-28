package io.micronaut.gcp.pubsub.bind;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.gcp.pubsub.annotation.OrderingKey;

import javax.inject.Singleton;

@Singleton
public class PubSubOrderingKeyBinder implements PubSubAnnotatedArgumentBinder<OrderingKey> {

	@Override
	public Class<OrderingKey> getAnnotationType() {	return OrderingKey.class; }

	@Override
	public BindingResult<Object> bind(ArgumentConversionContext<Object> context, PubSubConsumerState source) {
		return null;
	}
}
