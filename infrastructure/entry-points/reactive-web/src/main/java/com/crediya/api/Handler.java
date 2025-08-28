package com.crediya.api;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.api.dto.output.creditapplication.CreditApplicationApiResponse;
import com.crediya.api.mapper.CreditApplicationEntityMapper;
import com.crediya.api.mapper.CreditApplicationResponseMapper;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.usecase.createcreditapplication.CreateCreditApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.crediya.api.constants.ResponseMessage.CREDIT_APPLICATION_CREATED;
import static com.crediya.model.constants.ErrorMessage.NULL_CREDIT_APPLICATION;

@Component
@RequiredArgsConstructor
public class Handler {
	
	private final CreateCreditApplicationUseCase createCreditApplicationUseCase;
	
	public Mono<ServerResponse> createApplication(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(CreateCreditApplicationRequest.class)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(NULL_CREDIT_APPLICATION)))
			.map(CreditApplicationEntityMapper.INSTANCE::toEntity)
			.flatMap(createCreditApplicationUseCase::execute)
			.map(CreditApplicationResponseMapper.INSTANCE::toResponse)
			.flatMap(dtoResponse -> ServerResponse.status(HttpStatus.CREATED)
				.bodyValue(CreditApplicationApiResponse.of(
					dtoResponse,
					CREDIT_APPLICATION_CREATED
				)));
	}
}
