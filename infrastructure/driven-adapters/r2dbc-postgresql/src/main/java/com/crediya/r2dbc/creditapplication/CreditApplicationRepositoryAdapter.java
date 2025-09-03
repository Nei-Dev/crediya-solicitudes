package com.crediya.r2dbc.creditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.r2dbc.entities.CreditApplicationData;
import com.crediya.r2dbc.exceptions.StateNotFoundException;
import com.crediya.r2dbc.mappers.CreditApplicationEntityMapper;
import com.crediya.r2dbc.statecreditapplication.StateCreditApplicationReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import static com.crediya.r2dbc.constants.ErrorMessage.STATE_NOT_FOUND;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepository {
	
	private final CreditApplicationReactiveRepository creditApplicationRepository;
	private final StateCreditApplicationReactiveRepository stateRepository;
	private final TransactionalOperator transactionalOperator;
	
	@Override
	public Mono<CreditApplication> createApplication(CreditApplication creditApplication) {
		CreditApplicationData dataToSave = CreditApplicationEntityMapper.INSTANCE.toData(creditApplication);
		String state = creditApplication.getState().name();
		
		return transactionalOperator.transactional(
			stateRepository.findByName(state)
				.doOnSubscribe(subscription -> log.trace("Searching state in the database: {}", state))
				.doOnSuccess(stateRegistered -> log.info("State found in the database: {}", stateRegistered.getName()))
				.switchIfEmpty(Mono.error(new StateNotFoundException(STATE_NOT_FOUND)))
				.flatMap(stateData -> {
					dataToSave.setIdState(stateData.getId());
					return creditApplicationRepository.save(dataToSave)
						.doOnSubscribe(subscription -> log.trace("Creating credit application in the database: {}", dataToSave.getIdApplication()));
				})
				.flatMap(this::mapToEntity)
		);
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
}
