package com.crediya.api;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.api.dto.input.UpdateStateCreditApplication;
import com.crediya.api.dto.output.ApiResponse;
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
import com.crediya.usecase.updatestatecreditapplication.UpdateStateCreditApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.crediya.api.constants.PaginationParams.*;
import static com.crediya.api.constants.ResponseMessage.CREDIT_APPLICATION_CREATED;
import static com.crediya.api.constants.ResponseMessage.CREDIT_APPLICATION_UPDATED;
import static com.crediya.model.constants.CreateCreditApplicationErrorMessage.NULL_CREDIT_APPLICATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditApplicationHandler {
	
	private final ValidatorApi validatorApi;
	private final IAuthUseCase authUseCase;
	private final ICreateCreditApplicationUseCase createCreditApplicationUseCase;
	private final IGetCreditApplicationPaginatedUseCase getCreditApplicationPaginatedUseCase;
	private final UpdateStateCreditApplicationUseCase updateStateCreditApplicationUseCase;
	
	public Mono<ServerResponse> createApplication(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(CreateCreditApplicationRequest.class)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(NULL_CREDIT_APPLICATION)))
			.doOnSubscribe(subs -> log.trace("Starting create credit application request"))
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
				.doOnSuccess(res -> log.info("Credit application created with id {}", res.id()))
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
			.doOnNext(filter -> log.trace("Retrieving credit applications: {}", filter))
			.flatMap(getCreditApplicationPaginatedUseCase::execute)
			.doOnSuccess(pag -> log.info("Credit applications page {} retrieved with {} records", pag.getPage(), pag.getContent().size()))
			.flatMap(res -> ServerResponse.ok()
				.bodyValue(res)
			);
	}
	
	public Mono<ServerResponse> updateStateCreditApplication(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(UpdateStateCreditApplication.class)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(NULL_CREDIT_APPLICATION)))
			.doOnSubscribe(subs -> log.trace("Starting update state credit application request"))
			.flatMap(validatorApi::validate)
			.flatMap(req -> updateStateCreditApplicationUseCase.execute(req.idCreditApplication(), req.state()))
			.then(ServerResponse.ok().bodyValue(
				ApiResponse.of(CREDIT_APPLICATION_UPDATED)
			));
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
			.direction(sanitizeParamDirection(directionParam));
		
		if (statusParam != null) {
			builder.status(sanitizeParamStatus(statusParam));
		}
		
		if (autoEvalParam != null) {
			builder.autoEvaluation(Boolean.parseBoolean(autoEvalParam));
		}
		
		return Mono.just(builder.build());
	}
	
	private StateCreditApplication sanitizeParamStatus(String statusParam) {
		try {
			return StateCreditApplication.valueOf(statusParam.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	private SortDirection sanitizeParamDirection(String directionParam) {
		try {
			return SortDirection.valueOf(directionParam.toUpperCase());
		} catch (IllegalArgumentException e) {
			return SortDirection.ASC;
		}
	}
}
