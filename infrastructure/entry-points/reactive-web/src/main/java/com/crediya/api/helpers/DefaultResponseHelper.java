package com.crediya.api.helpers;

import com.crediya.api.dto.output.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultResponseHelper {
	
	private final ObjectMapper objectMapper;

	public Mono<Void> writeJsonResponse (ServerHttpResponse serverHttpResponse, ErrorResponse errorResponse) {
		serverHttpResponse.setStatusCode(HttpStatus.valueOf(errorResponse.getCode()));
		serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		try {
			byte[] json = objectMapper.writeValueAsBytes(errorResponse);
			return serverHttpResponse.writeWith(Mono.just(serverHttpResponse
				.bufferFactory().wrap(json)));
		} catch (Exception e) {
			log.error("Error serializing security error response: {}", e.getMessage(), e);
			serverHttpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return serverHttpResponse.setComplete();
		}
	}

}
