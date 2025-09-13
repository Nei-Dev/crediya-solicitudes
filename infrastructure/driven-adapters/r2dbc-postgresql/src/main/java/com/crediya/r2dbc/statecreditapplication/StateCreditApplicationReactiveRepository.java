package com.crediya.r2dbc.statecreditapplication;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface StateCreditApplicationReactiveRepository extends ReactiveCrudRepository<StateCreditApplicationData, Long>, ReactiveQueryByExampleExecutor<StateCreditApplicationData> {
	
	Mono<StateCreditApplicationData> findByName(String name);
	
}
