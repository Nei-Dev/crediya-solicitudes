package com.crediya.r2dbc.creditapplication;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.CreditApplicationSummary;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.r2dbc.entities.CreditApplicationData;
import com.crediya.r2dbc.exceptions.StateNotFoundException;
import com.crediya.r2dbc.mappers.CreditApplicationEntityMapper;
import com.crediya.r2dbc.statecreditapplication.StateCreditApplicationReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;

import static com.crediya.r2dbc.constants.CreditApplicationPaginationConstants.*;
import static com.crediya.r2dbc.constants.ErrorMessage.STATE_NOT_FOUND;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepository {
	
	private final CreditApplicationReactiveRepository creditApplicationRepository;
	private final StateCreditApplicationReactiveRepository stateRepository;
	private final TransactionalOperator transactionalOperator;
	private final DatabaseClient databaseClient;

	private static final String COUNT_QUERY = """
	    SELECT COUNT(*)
	    FROM application app
	    JOIN credit_type ct ON ct.id_credit_type = app.id_credit_type
	    JOIN state st ON st.id_state = app.id_state
	    WHERE (:stateApplication IS NULL OR st.name = :stateApplication)
	    AND (:isAutoEvaluation IS NULL OR ct.auto_validation = :isAutoEvaluation)
	""";
	
	@Override
	public Mono<CreditApplication> createApplication(CreditApplication creditApplication) {
		CreditApplicationData dataToSave = CreditApplicationEntityMapper.INSTANCE.toData(creditApplication);
		String state = creditApplication.getState().name();
		
		return transactionalOperator.transactional(
			stateRepository.findByName(state)
				.doOnSubscribe(subscription -> log.trace("Searching state in the database: {}", state))
				.doOnSuccess(stateRegistered -> log.info("State found in the database: {}", stateRegistered.getName()))
				.switchIfEmpty(Mono.error(new StateNotFoundException(STATE_NOT_FOUND)))
				.flatMap(stateData -> {
					dataToSave.setIdState(stateData.getId());
					return creditApplicationRepository.save(dataToSave)
						.doOnSubscribe(subscription -> log.trace("Creating credit application in the database"));
				})
				.flatMap(this::mapToEntity)
		);
	}
	
	@Override
	public Mono<PaginationResponse<CreditApplicationSummary>> getAllApplications(PaginationCreditApplicationFilter filter) {
		int offset = (filter.getPage() - 1) * filter.getSize();
		String sortBy = sanitizeSortBy(filter.getSortBy());
		String sortDirection = filter.getDirection().name();

		Flux<CreditApplicationSummary> content = this.getContent(
			sortBy,
			sortDirection,
			filter,
			offset
		);

		Mono<Long> total = this.getTotalCount(filter);

		return Mono.zip(
				content.collectList(),
				total
			)
			.doOnSubscribe(subs -> log.trace("Retrieving paginated credit applications from database with filter: {}", filter))
			.doOnSuccess(res -> log.debug("Result paginated retrieved with {}", res.getT1().size()))
			.map(tuple -> {
				long totalElements = tuple.getT2();
				int totalPages = (int) Math.ceil((double) totalElements / filter.getSize());
				return PaginationResponse.<CreditApplicationSummary>builder()
					.content(tuple.getT1())
					.page(filter.getPage())
					.size(filter.getSize())
					.totalElements(totalElements)
					.totalPages(totalPages)
					.build();
			});
	}
	
	private Mono<CreditApplication> mapToEntity(CreditApplicationData data) {
		if (data == null) {
			return Mono.empty();
		}
		return stateRepository.findById(data.getIdState())
			.switchIfEmpty(Mono.error(new StateNotFoundException(STATE_NOT_FOUND)))
			.map(stateData -> {
				CreditApplication entity = CreditApplicationEntityMapper.INSTANCE.toEntity(data);
				entity.setState(StateCreditApplication.valueOf(stateData.getName()));
				return entity;
			});
	}
	
	private DatabaseClient.GenericExecuteSpec binNullParams(DatabaseClient.GenericExecuteSpec spec, PaginationCreditApplicationFilter filter) {
		if (Objects.nonNull(filter.getStatus())) {
			spec = spec.bind(PARAM_STATE_APPLICATION, filter.getStatus().name());
		} else {
			spec = spec.bindNull(PARAM_STATE_APPLICATION, String.class);
		}
		if (Objects.nonNull(filter.getAutoEvaluation())) {
			spec = spec.bind(PARAM_IS_AUTO_EVALUATION, filter.getAutoEvaluation());
		} else {
			spec = spec.bindNull(PARAM_IS_AUTO_EVALUATION, Boolean.class);
		}
		return spec;
	}

	private Flux<CreditApplicationSummary> getContent(String sortBy, String sortDirection, PaginationCreditApplicationFilter filter, int offset) {
		DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(buildPaginatedQuery(sortBy, sortDirection));
		spec = this.binNullParams(spec, filter);
		
		return spec
			.bind(PARAM_LIMIT, filter.getSize())
			.bind(PARAM_OFFSET, offset)
			.map((row, metadata) -> CreditApplicationSummary.builder()
				.amount(row.get(COL_AMOUNT, BigDecimal.class))
				.term(row.get(COL_TERM, Integer.class))
				.email(row.get(COL_EMAIL, String.class))
				.clientName(row.get(COL_CLIENT_NAME, String.class))
				.creditType(row.get(COL_CREDIT_TYPE, String.class))
				.interestRate(row.get(COL_INTEREST_RATE, BigDecimal.class))
				.stateApplication(row.get(COL_STATE_APPLICATION, String.class))
				.salaryBase(row.get(COL_SALARY_BASE, BigDecimal.class))
				.monthlyAmount(row.get(COL_MONTHLY_AMOUNT, BigDecimal.class))
				.build()
			)
			.all();
	}
	
	private Mono<Long> getTotalCount(PaginationCreditApplicationFilter filter) {
		DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(COUNT_QUERY);
		spec = this.binNullParams(spec, filter);
		
		return spec
			.map((row, metadata) -> row.get(0, Long.class))
			.one();
	}
	
	@SuppressWarnings("squid:S2077")
	private String buildPaginatedQuery(String sortBy, String sortDirection) {
		return String.format("""
				SELECT
					app.amount,
					app.term,
					app.email,
					app.client_name AS clientName,
					ct.name AS creditType,
					ct.interest_rate AS interestRate,
					st.name AS stateApplication,
					app.client_salary_base AS salaryBase,
					ROUND((app.amount + (app.amount * (ct.interest_rate / 100) * (app.term / 12.0))) / app.term, 2) AS monthlyAmount
				FROM application app
				JOIN credit_type ct ON ct.id_credit_type = app.id_credit_type
				JOIN state st ON st.id_state = app.id_state
				WHERE (:stateApplication IS NULL OR st.name = :stateApplication)
				AND (:isAutoEvaluation IS NULL OR ct.auto_validation = :isAutoEvaluation)
				ORDER BY %s %s
				LIMIT :limit OFFSET :offset
			""", sortBy, sortDirection);
	}
	
	private String sanitizeSortBy(String sortBy) {
		return switch (sortBy) {
			case COL_AMOUNT,
			     COL_TERM,
			     COL_EMAIL,
			     COL_CLIENT_NAME,
			     COL_CREDIT_TYPE,
			     COL_INTEREST_RATE,
			     COL_STATE_APPLICATION,
			     COL_SALARY_BASE,
			     COL_MONTHLY_AMOUNT -> sortBy;
			default -> COL_AMOUNT;
		};
	}
	
}
