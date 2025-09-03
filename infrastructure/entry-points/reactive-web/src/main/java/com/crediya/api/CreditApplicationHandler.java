package com.crediya.api;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.api.dto.output.creditapplication.CreditApplicationApiResponse;
import com.crediya.api.helpers.ValidatorApi;
import com.crediya.api.mapper.CreditApplicationEntityMapper;
import com.crediya.api.mapper.CreditApplicationResponseMapper;
import com.crediya.model.auth.UserClaims;
import com.crediya.model.creditapplication.ports.ICreateCreditApplicationUseCase;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.crediya.api.constants.ResponseMessage.CREDIT_APPLICATION_CREATED;
import static com.crediya.model.constants.ErrorMessage.NULL_CREDIT_APPLICATION;
import static com.crediya.model.constants.ErrorMessage.USER_NOT_MATCH;

@Component
@RequiredArgsConstructor
public class CreditApplicationHandler {
	
	private final ValidatorApi validatorApi;
	private final ICreateCreditApplicationUseCase createCreditApplicationUseCase;
	
	public Mono<ServerResponse> createApplication(ServerRequest serverRequest) {
		return serverRequest.principal()
			.cast(UsernamePasswordAuthenticationToken.class)
			.map(auth -> ( UserClaims ) auth.getPrincipal())
			.flatMap(userClaims -> serverRequest.bodyToMono(CreateCreditApplicationRequest.class)
				.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(NULL_CREDIT_APPLICATION)))
				.flatMap(validatorApi::validate)
				.filter(app -> Objects.equals(app.email(), userClaims.email())
					&& Objects.equals(app.identification(), userClaims.identification())
				)
				.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(USER_NOT_MATCH)))
				.map(CreditApplicationEntityMapper.INSTANCE::toEntity)
				.flatMap(createCreditApplicationUseCase::execute)
				.map(CreditApplicationResponseMapper.INSTANCE::toResponse)
				.flatMap(dtoResponse -> ServerResponse.status(HttpStatus.CREATED)
					.bodyValue(CreditApplicationApiResponse.of(
						dtoResponse,
						CREDIT_APPLICATION_CREATED
					))
				)
			);
	}
}
