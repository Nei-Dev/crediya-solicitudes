package com.crediya.r2dbc.creditapplication;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<Object, String>, ReactiveQueryByExampleExecutor<Object> {

}
