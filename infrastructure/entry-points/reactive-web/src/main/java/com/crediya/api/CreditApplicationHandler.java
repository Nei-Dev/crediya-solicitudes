package com.crediya.api;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.api.dto.output.creditapplication.CreditApplicationApiResponse;
import com.crediya.api.helpers.ValidatorApi;
import com.crediya.api.mapper.CreditApplicationEntityMapper;
import com.crediya.api.mapper.CreditApplicationResponseMapper;
import com.crediya.model.auth.User;
import com.crediya.model.auth.ports.IAuthUseCase;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.ports.ICreateCreditApplicationUseCase;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.crediya.api.constants.ResponseMessage.CREDIT_APPLICATION_CREATED;
import static com.crediya.model.constants.ErrorMessage.NULL_CREDIT_APPLICATION;

@Component
@RequiredArgsConstructor
public class CreditApplicationHandler {
	
	private final ValidatorApi validatorApi;
	private final ICreateCreditApplicationUseCase createCreditApplicationUseCase;
	private final IAuthUseCase authUseCase;
	
	public Mono<ServerResponse> createApplication(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(CreateCreditApplicationRequest.class)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(NULL_CREDIT_APPLICATION)))
			.flatMap(validatorApi::validate)
			.map(CreditApplicationEntityMapper.INSTANCE::toEntity)
			.flatMap(app -> serverRequest.principal()
				.cast(UsernamePasswordAuthenticationToken.class)
				.map(auth -> ( String ) auth.getCredentials())
				.flatMap(authUseCase::getUserByToken)
				.flatMap(user -> enrichWithUserData(app, user))
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
	
	private Mono<CreditApplication> enrichWithUserData(CreditApplication application, User user) {
		application.setEmail(user.getEmail());
		application.setIdentification(user.getIdentification());
		application.setClientName(user.getFullname());
		application.setClientSalaryBase(user.getSalaryBase());
		return Mono.just(application);
	}
}
