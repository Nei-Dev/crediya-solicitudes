package com.crediya.r2dbc.creditapplication;

import com.crediya.r2dbc.entities.CreditApplicationData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<CreditApplicationData, Long>, ReactiveQueryByExampleExecutor<CreditApplicationData> {

	@Query("""
		SELECT
			SUM(
				ROUND(
					app.amount
					    *
					(((ct.interest_rate / 100) / 12) * (1 + ((ct.interest_rate / 100) / 12))**app.term)
				    	/
				    ((1 + ((ct.interest_rate / 100) / 12))**app.term - 1),
				    2
			    )
			)
		FROM application app
		JOIN state st ON app.id_state = st.id_state
		JOIN credit_type ct ON ct.id_credit_type = app.id_credit_type
		WHERE app.email = :email
	""")
	Mono<BigDecimal> findTotalMonthlyDebt(String email);
	
}
