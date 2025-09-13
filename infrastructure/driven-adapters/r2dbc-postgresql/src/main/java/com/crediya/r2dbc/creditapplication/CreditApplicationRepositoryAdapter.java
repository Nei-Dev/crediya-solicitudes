package com.crediya.r2dbc.creditapplication;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.CreditApplicationSummary;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.helpers.CalculateAmortizingLoan;
import com.crediya.model.helpers.SortDirection;
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

	@Override
	public Mono<CreditApplication> saveCreditApplication(CreditApplication creditApplication) {
		CreditApplicationData dataToSave = CreditApplicationEntityMapper.INSTANCE.toData(creditApplication);
		String state = creditApplication.getState().name();
		
		return transactionalOperator.transactional(
			stateRepository.findByName(state)
				.doOnSubscribe(subscription -> log.trace("Searching state in the database: {}", state))
				.doOnSuccess(stateRegistered -> log.debug("State found in the database: {}", stateRegistered.getName()))
				.switchIfEmpty(Mono.error(new StateNotFoundException(STATE_NOT_FOUND)))
				.flatMap(stateData -> {
					dataToSave.setIdState(stateData.getId());
					return creditApplicationRepository.save(dataToSave)
						.doOnSubscribe(subscription -> log.trace("Saving credit application in the database"));
				})
				.flatMap(this::mapToEntity)
		);
	}
	
	@Override
	public Mono<PaginationResponse<CreditApplicationSummary>> getAllApplications(PaginationCreditApplicationFilter filter) {
		int offset = (filter.getPage() - 1) * filter.getSize();

		Flux<CreditApplicationSummary> content = this.getContent(
			filter.getSortBy(),
			filter.getDirection(),
			filter,
			offset
		);

		Mono<Long> total = this.getTotalCount(filter);

		return Mono.zip(
				content.collectList(),
				total
			)
			.doOnSubscribe(subs -> log.trace("Retrieving paginated credit applications from database with filter: {}", filter))
			.doOnSuccess(res -> log.info("Result paginated retrieved with {}", res.getT1().size()))
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
	
	@Override
	public Mono<CreditApplication> findById(Long id) {
		return creditApplicationRepository.findById(id)
			.doOnSubscribe(subscription -> log.trace("Searching credit application by id: {}", id))
			.switchIfEmpty(Mono.empty())
			.doOnSuccess(creditApplicationData -> log.info("Credit application found with id: {}", id))
			.flatMap(this::mapToEntity);
	}
	
	@Override
	public Mono<BigDecimal> findTotalMonthlyDebt(String email) {
		return creditApplicationRepository.findAllApprovedByEmail(email)
			.doOnSubscribe(subscription -> log.trace("Calculating total monthly debt for email: {}", email))
			.map(data -> CalculateAmortizingLoan.calculateMonthlyPayment(
				data.amount(),
				data.interest_rate(),
				data.term())
			)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.defaultIfEmpty(BigDecimal.ZERO)
			.doOnError(error -> log.error("Error calculating total monthly debt for email {}: {}", email, error.getMessage()))
			.doOnSuccess(totalDebt -> log.info("Total monthly debt for email {}: {}", email, totalDebt));
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
	
	private Flux<CreditApplicationSummary> getContent(String sortBy, SortDirection sortDirection, PaginationCreditApplicationFilter filter, int offset) {
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(
            new CreditApplicationQueryBuilder()
                .selectPaginatedFields()
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build()
        );
        spec = CreditApplicationQueryBuilder.bindParams(spec, filter);
		
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
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(
            new CreditApplicationQueryBuilder()
                .selectCount()
                .build()
        );
        spec = CreditApplicationQueryBuilder.bindParams(spec, filter);
		
		return spec
			.map((row, metadata) -> row.get(0, Long.class))
			.one();
	}
	
}
