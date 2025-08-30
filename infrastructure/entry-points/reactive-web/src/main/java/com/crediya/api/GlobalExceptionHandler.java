package com.crediya.api;

import com.crediya.api.dto.output.ErrorResponse;
import com.crediya.model.exceptions.BusinessException;
import com.crediya.model.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.crediya.api.constants.ErrorMessage.GENERIC_ERROR;

@Slf4j
@Component
public class GlobalExceptionHandler implements WebExceptionHandler {
	
	private final ObjectMapper objectMapper;

	private final Map<Class<? extends RuntimeException>, BiFunction<Throwable, ServerWebExchange, ErrorResponse>> handlers = new HashMap<>();

	public GlobalExceptionHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		handlers.put(BusinessException.class, (ex, exchange) ->
			ErrorResponse.badRequest(ex.getMessage()));

		handlers.put(NotFoundException.class, (ex, exchange) ->
			ErrorResponse.notFound(ex.getMessage()));

		handlers.put(WebExchangeBindException.class, (ex, exchange) -> {
			String errors = ((WebExchangeBindException) ex).getBindingResult()
				.getAllErrors()
				.stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.distinct()
				.collect(Collectors.joining("; "));
			return ErrorResponse.badRequest(errors);
		});

		handlers.put(NoResourceFoundException.class, (ex, exchange) ->
			ErrorResponse.notFound("Resource not found"));
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

		HttpStatus status = HttpStatus.valueOf(response.getCode());

		exchange.getResponse().setStatusCode(status);
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		try {
			byte[] json = objectMapper.writeValueAsBytes(response);
			return exchange.getResponse()
				.writeWith(Mono.just(exchange.getResponse()
					.bufferFactory().wrap(json)));
		} catch (Exception e) {
			log.error("Error serializing ErrorResponse: {}", e.getMessage(), e);
			exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return exchange.getResponse().setComplete();
		}
	}
	
}
