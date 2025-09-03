package com.crediya.api;

import com.crediya.api.dto.output.ErrorResponse;
import com.crediya.api.helpers.DefaultResponseHelper;
import com.crediya.model.exceptions.BusinessException;
import com.crediya.model.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static com.crediya.api.constants.ErrorMessage.*;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class GlobalExceptionHandler implements WebExceptionHandler {
	
	private final DefaultResponseHelper defaultResponseHelper;
	
	private final Map<Class<? extends Throwable>, BiFunction<Throwable, ServerWebExchange, ErrorResponse>> handlers = new HashMap<>();
	
	public GlobalExceptionHandler(DefaultResponseHelper defaultResponseHelper) {
		this.defaultResponseHelper = defaultResponseHelper;
		
		handlers.put(BusinessException.class, (ex, exchange) ->
			ErrorResponse.badRequest(ex.getMessage()));
		
		handlers.put(NotFoundException.class, (ex, exchange) ->
			ErrorResponse.notFound(ex.getMessage()));
		
		handlers.put(NoResourceFoundException.class, (ex, exchange) ->
			ErrorResponse.notFound(NOT_FOUND));
		
		handlers.put(
			AccessDeniedException.class, (ex, exchange) ->
				ErrorResponse.forbidden(ACCESS_DENIED));
		
		handlers.put(
			AuthenticationException.class, (ex, exchange) ->
				ErrorResponse.unauthorized(UNAUTHORIZED));
	}
	
	@NonNull
	@Override
	public Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable ex) {
		ErrorResponse response = handlers.entrySet().stream()
			.filter(e -> e.getKey().isAssignableFrom(ex.getClass()))
			.findFirst()
			.map(e -> e.getValue().apply(ex, exchange))
			.orElseGet(() -> {
				log.error("Unhandled exception: {}", ex.getMessage(), ex);
				return ErrorResponse.internalServerError(GENERIC_ERROR);
			});
		
		return defaultResponseHelper.writeJsonResponse(exchange.getResponse(), response);
	}
	
}
