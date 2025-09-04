package com.crediya.api.config;

import jakarta.annotation.PostConstruct;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import static com.crediya.api.constants.CorrelationConstants.CORRELATION_ID_KEY;

@Configuration
public class CorrelationConfig {
	
    @PostConstruct
    public void setupCorrelationContext() {
        Hooks.enableAutomaticContextPropagation();
        setup();
    }
	
	private void setup() {
		Hooks.onEachOperator("mdc", Operators.lift((sc, subscriber) -> new CoreSubscriber<>() {
			@Override
			public void onSubscribe(@NonNull Subscription s) {
				subscriber.onSubscribe(s);
			}
			
			@Override
			public void onNext(Object o) {
				Context context = currentContext();
				String correlationId = context.getOrDefault(CORRELATION_ID_KEY, null);
				if (correlationId != null) {
					MDC.put(CORRELATION_ID_KEY, correlationId);
				} else {
					MDC.remove(CORRELATION_ID_KEY);
				}
				subscriber.onNext(o);
			}
			
			@Override
			public void onError(Throwable t) {
				MDC.remove(CORRELATION_ID_KEY);
				subscriber.onError(t);
			}
			
			@Override
			public void onComplete() {
				MDC.remove(CORRELATION_ID_KEY);
				subscriber.onComplete();
			}
			
			@NonNull
			@Override
			public Context currentContext() {
				return subscriber.currentContext();
			}
		}));
	}
}

