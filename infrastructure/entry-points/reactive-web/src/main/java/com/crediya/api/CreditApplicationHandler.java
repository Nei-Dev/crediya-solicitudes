package com.crediya.api;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.api.dto.output.creditapplication.CreditApplicationApiResponse;
import com.crediya.api.helpers.ValidatorApi;
import com.crediya.api.mapper.CreditApplicationEntityMapper;
import com.crediya.api.mapper.CreditApplicationResponseMapper;
import com.crediya.model.auth.User;
import com.crediya.model.auth.ports.IAuthUseCase;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.ports.ICreateCreditApplicationUseCase;
import com.crediya.model.creditapplication.ports.IGetCreditApplicationPaginatedUseCase;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.helpers.SortDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.crediya.api.constants.PaginationParams.*;
import static com.crediya.api.constants.ResponseMessage.CREDIT_APPLICATION_CREATED;
import static com.crediya.model.constants.CreateCreditApplicationErrorMessage.NULL_CREDIT_APPLICATION;

@Component
@RequiredArgsConstructor
public class CreditApplicationHandler {
	
	private final ValidatorApi validatorApi;
	private final ICreateCreditApplicationUseCase createCreditApplicationUseCase;
	private final IGetCreditApplicationPaginatedUseCase getCreditApplicationPaginatedUseCase;
	private final IAuthUseCase authUseCase;
	
	public Mono<ServerResponse> createApplication(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(CreateCreditApplicationRequest.class)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(NULL_CREDIT_APPLICATION)))
			.flatMap(validatorApi::validate)
			.map(CreditApplicationEntityMapper.INSTANCE::toEntity)
			.flatMap(app -> serverRequest.principal()
				.cast(UsernamePasswordAuthenticationToken.class)
				.map(auth -> ( String ) auth.getCredentials())
				.log()
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
	
	public Mono<ServerResponse> getAllApplications(ServerRequest serverRequest) {
		return buildFilter(serverRequest)
			.flatMap(getCreditApplicationPaginatedUseCase::execute)
			.flatMap(res -> ServerResponse.ok()
				.bodyValue(res)
			);
	}
	
	private Mono<CreditApplication> enrichWithUserData(CreditApplication application, User user) {
		application.setEmail(user.getEmail());
		application.setIdentification(user.getIdentification());
		application.setClientName(user.getFullname());
		application.setClientSalaryBase(user.getSalaryBase());
		return Mono.just(application);
	}
	
	private Mono<PaginationCreditApplicationFilter> buildFilter(ServerRequest serverRequest) {
		int page = serverRequest.queryParam(PARAM_PAGE).map(Integer::parseInt).orElse(DEFAULT_PAGE);
		int size = serverRequest.queryParam(PARAM_SIZE).map(Integer::parseInt).orElse(DEFAULT_SIZE);
		String sortBy = serverRequest.queryParam(PARAM_SORT_BY).orElse(DEFAULT_SORT_BY);
		String directionParam = serverRequest.queryParam(PARAM_DIRECTION).orElse(DEFAULT_DIRECTION);
		String statusParam = serverRequest.queryParam(PARAM_STATUS).orElse(null);
		String autoEvalParam = serverRequest.queryParam(PARAM_AUTO_EVALUATION).orElse(null);
		
		PaginationCreditApplicationFilter.PaginationCreditApplicationFilterBuilder builder = PaginationCreditApplicationFilter.builder()
			.page(page)
			.size(size)
			.sortBy(sortBy)
			.direction(sanitizeDirection(directionParam));
		
		if (statusParam != null) {
			builder.status(sanitizeStatus(statusParam));
		}
		
		if (autoEvalParam != null) {
			builder.autoEvaluation(Boolean.parseBoolean(autoEvalParam));
		}
		
		return Mono.just(builder.build());
	}
	
	private StateCreditApplication sanitizeStatus(String statusParam) {
		try {
			return StateCreditApplication.valueOf(statusParam.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	private SortDirection sanitizeDirection(String directionParam) {
		try {
			return SortDirection.valueOf(directionParam.toUpperCase());
		} catch (IllegalArgumentException e) {
			return SortDirection.ASC;
		}
	}
}
