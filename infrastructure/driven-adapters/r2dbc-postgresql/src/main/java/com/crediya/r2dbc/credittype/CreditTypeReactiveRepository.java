package com.crediya.r2dbc.credittype;

import com.crediya.r2dbc.entities.CreditTypeData;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CreditTypeReactiveRepository extends ReactiveCrudRepository<CreditTypeData, Long>, ReactiveQueryByExampleExecutor<CreditTypeData> {
}
