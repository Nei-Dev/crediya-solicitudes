package com.crediya.r2dbc.creditapplication;

import com.crediya.r2dbc.projections.CreditApplicationDataDebt;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<CreditApplicationData, Long>, ReactiveQueryByExampleExecutor<CreditApplicationData> {

	@Query("""
		SELECT
			app.id_application AS id_credit_application,
			app.amount AS amount,
			app.term AS term,
			ct.interest_rate AS interest_rate,
			app.client_salary_base AS salary_base
		FROM application app
		JOIN state st ON app.id_state = st.id_state
		JOIN credit_type ct ON ct.id_credit_type = app.id_credit_type
		WHERE app.email = :email AND st.name = 'APPROVED'
	""")
	Flux<CreditApplicationDataDebt> findAllApprovedByEmail(String email);
	
}
