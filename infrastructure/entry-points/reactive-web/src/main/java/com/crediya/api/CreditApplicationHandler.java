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
import com.crediya.model.creditapplication.ports.ICalculateCapacityUseCase;
import com.crediya.model.creditapplication.ports.ICreateCreditApplicationUseCase;
import com.crediya.model.creditapplication.ports.IGetCreditApplicationPaginatedUseCase;
import com.crediya.model.creditapplication.ports.IUpdateStateCreditApplicationUseCase;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.helpers.SortDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.crediya.api.constants.PaginationParams.*;
import static com.crediya.api.constants.PathVariable.ID_CREDIT_APPLICATION;
import static com.crediya.api.constants.ResponseMessage.*;
import static com.crediya.model.constants.CreateCreditApplicationErrorMessage.NULL_CREDIT_APPLICATION;
import static com.crediya.model.constants.CreateCreditApplicationErrorMessage.USER_NOT_MATCH;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditApplicationHandler {
	
	private final ValidatorApi validatorApi;
	private final IAuthUseCase authUseCase;
	private final ICreateCreditApplicationUseCase createCreditApplicationUseCase;
	private final IGetCreditApplicationPaginatedUseCase getCreditApplicationPaginatedUseCase;
	private final IUpdateStateCreditApplicationUseCase updateStateCreditApplicationUseCase;
	private final ICalculateCapacityUseCase calculateCapacityApplicationUseCase;
	
	public Mono<ServerResponse> createApplication(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(CreateCreditApplicationRequest.class)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(NULL_CREDIT_APPLICATION)))
			.doOnSubscribe(subs -> log.trace("Starting create credit application request"))
			.flatMap(validatorApi::validate)
			.map(CreditApplicationEntityMapper.INSTANCE::toEntity)
			.flatMap(app -> serverRequest.principal()
				.cast(UsernamePasswordAuthenticationToken.class)
				.map(auth -> ( String ) auth.getCredentials())
				.flatMap(authUseCase::getUserByToken)
				.flatMap(user -> this.validateSameUser(app, user))
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
			.flatMap(req -> updateStateCreditApplicationUseCase.execute(req.idCreditApplication(), sanitizeStatus(req.state())))
			.then(ServerResponse.ok().bodyValue(
				ApiResponse.of(CREDIT_APPLICATION_UPDATED)
			));
	}
	
	public Mono<ServerResponse> calculateCapacityCreditApplication(ServerRequest serverRequest) {
		return Mono.fromCallable(() -> serverRequest.pathVariable(ID_CREDIT_APPLICATION))
			.map(Long::parseLong)
			.doOnSubscribe(subs -> log.trace("Starting calculate capacity credit application request"))
			.flatMap(calculateCapacityApplicationUseCase::execute)
			.then(ServerResponse.ok().bodyValue(
				ApiResponse.of(CAPACITY_CALCULATED)
			));
	}
	
	private Mono<User> validateSameUser(CreditApplication application, User user) {
		return Mono.just(user)
			.filter(u -> application.getIdentification().equals(u.getIdentification()))
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(USER_NOT_MATCH)))
			.filter(u -> application.getEmail().equals(u.getEmail()))
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(USER_NOT_MATCH)));
	}
	
	private Mono<CreditApplication> enrichWithUserData(CreditApplication application, User user) {
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
