package com.crediya.r2dbc.credittype;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CreditTypeReactiveRepository  extends ReactiveCrudRepository<Object, String>, ReactiveQueryByExampleExecutor<Object> {
}
