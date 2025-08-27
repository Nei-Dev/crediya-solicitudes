package com.crediya.r2dbc.creditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.r2dbc.entities.CreditApplicationData;
import com.crediya.r2dbc.mappers.CreditApplicationEntityMapper;
import com.crediya.r2dbc.mappers.StateCreditApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepository {
	
	private final CreditApplicationReactiveRepository repository;
	private final TransactionalOperator transactionalOperator;
	
	@Override
	public Mono<CreditApplication> createApplication(CreditApplication creditApplication) {
		CreditApplicationData dataToSave = CreditApplicationEntityMapper.INSTANCE.toData(creditApplication);
		String state = StateCreditApplicationMapper.toDatabase(creditApplication.getState());
		return null;
	}
}
