package com.crediya.api.filters;

import com.crediya.api.config.CorrelationConstants;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
public class CorrelationWebFilter implements WebFilter {
	
	@NonNull
	@Override
	public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();

		String correlationIdInHeader = request.getHeaders().getFirst(CorrelationConstants.X_CORRELATION_ID);
		String correlationId = correlationIdInHeader != null && !correlationIdInHeader.isBlank()
			? correlationIdInHeader
			: generateCorrelationId();

		response.getHeaders().add(CorrelationConstants.X_CORRELATION_ID, correlationId);

		return chain.filter(exchange)
			.contextWrite(Context.of(CorrelationConstants.CORRELATION_ID_KEY, correlationId));
	}
	
	private String generateCorrelationId() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, CorrelationConstants.CORRELATION_ID_LENGTH);
	}
}
