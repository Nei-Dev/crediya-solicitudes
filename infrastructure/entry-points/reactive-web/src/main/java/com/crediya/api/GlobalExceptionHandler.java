package com.crediya.api;

import com.crediya.api.dto.output.ErrorResponse;
import com.crediya.model.exceptions.BusinessException;
import com.crediya.model.exceptions.NotFoundException;
import com.crediya.model.exceptions.TechnicalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static com.crediya.api.constants.ErrorMessage.GENERIC_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler({ Exception.class, TechnicalException.class })
	public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
		log.error(
			"Unhandled exception occurred: {}",
			ex.getMessage(),
			ex
		);
		return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ErrorResponse.internalServerError(GENERIC_ERROR)));
	}
	
	@ExceptionHandler(BusinessException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleBusinessException(BusinessException ex) {
		ErrorResponse response = ErrorResponse.badRequest(ex.getMessage());
		return Mono.just(ResponseEntity.status(response.getCode()).contentType(MediaType.APPLICATION_JSON).body(response));
	}

	@ExceptionHandler(WebExchangeBindException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleBindException(WebExchangeBindException ex) {
		String errors = ex.getBindingResult()
				.getAllErrors()
				.stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.distinct()
				.collect(Collectors.joining("; "));
		ErrorResponse response = ErrorResponse.badRequest(errors);
		return Mono.just(ResponseEntity.status(response.getCode()).contentType(MediaType.APPLICATION_JSON).body(response));
	}
	
	@ExceptionHandler(NotFoundException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleBindException(NotFoundException ex) {
		ErrorResponse response = ErrorResponse.notFound(ex.getMessage());
		return Mono.just(ResponseEntity.status(response.getCode()).contentType(MediaType.APPLICATION_JSON).body(response));
	}
	
}
