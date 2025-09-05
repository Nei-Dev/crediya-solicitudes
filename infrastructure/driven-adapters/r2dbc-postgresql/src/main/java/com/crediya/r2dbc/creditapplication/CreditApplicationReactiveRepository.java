package com.crediya.r2dbc.creditapplication;

import com.crediya.model.projection.CreditApplicationProjection;
import com.crediya.r2dbc.entities.CreditApplicationData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<CreditApplicationData, Long>, ReactiveQueryByExampleExecutor<CreditApplicationData> {
	
	@Query("""
		SELECT
		    app.amount,
		    app.term,
		    app.email,
		    app.client_name AS clientName,
		    ct.name AS creditType,
		    ct.interest_rate AS interestRate,
		    st.name AS stateApplication,
		    app.client_salary_base AS salaryBase
		FROM application app
		JOIN credit_type ct ON ct.id_credit_type = app.id_credit_type
		JOIN state st ON st.id_state = app.id_state
		WHERE (st.name = :stateApplication)
		AND (ct.auto_validation = :isAutoEvaluation)
		ORDER BY :orderBy
		LIMIT :limit OFFSET :offset
	""")
	Flux<CreditApplicationProjection> findCreditsWithDetails(
		int limit,
		int offset,
		String orderBy,
		String orderDirection,
		String stateApplication,
		Boolean isAutoEvaluation
	);

}
