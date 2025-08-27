package com.crediya.r2dbc.creditapplication;

import com.crediya.r2dbc.entities.CreditApplicationData;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<CreditApplicationData, Long>, ReactiveQueryByExampleExecutor<CreditApplicationData> {

}
