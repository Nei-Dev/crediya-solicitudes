package com.crediya.usecase.updatestatecreditapplication;

import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.gateways.MessageService;
import com.crediya.model.creditapplication.ports.IUpdateStateCreditApplicationUseCase;
import com.crediya.model.exceptions.creditapplication.CreditApplicationNotFoundException;
import com.crediya.model.exceptions.statecreditapplication.InvalidStateCreditApplication;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.CREDIT_APPLICATION_NOT_FOUND;
import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.INVALID_ID_CREDIT_APPLICATION;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.*;

@RequiredArgsConstructor
public class UpdateStateCreditApplicationUseCase implements IUpdateStateCreditApplicationUseCase {
	
	private final CreditApplicationRepository repository;
	private final MessageService messageService;
	
	@Override
	public Mono<Void> execute(Long idCreditApplication, StateCreditApplication state) {
		return this.validateState(state)
			.flatMap(newSate -> this.validateIdCreditApplication(idCreditApplication)
				.flatMap(repository::findById)
				.switchIfEmpty(Mono.error(new CreditApplicationNotFoundException(CREDIT_APPLICATION_NOT_FOUND)))
				.flatMap(creditApplication -> {
					if (creditApplication.getState() != StateCreditApplication.PENDING) {
						return Mono.error(new InvalidStateCreditApplication(STATE_CANNOT_BE_MODIFIED));
					}
					creditApplication.setState(newSate);
					return repository.saveCreditApplication(creditApplication);
				}))
			.flatMap(messageService::sendChangeStateCreditApplication)
			.then();
	}
	
	private Mono<Long> validateIdCreditApplication(Long idCreditApplication) {
		return Mono.justOrEmpty(idCreditApplication)
			.switchIfEmpty(Mono.error(new InvalidStateCreditApplication(INVALID_ID_CREDIT_APPLICATION)))
			.filter(id -> id > 0)
			.switchIfEmpty(Mono.error(new InvalidStateCreditApplication(INVALID_ID_CREDIT_APPLICATION)));
	}
	
	private Mono<StateCreditApplication> validateState(StateCreditApplication state) {
		return Mono.justOrEmpty(state)
			.switchIfEmpty(Mono.error(new InvalidStateCreditApplication(STATE_NOT_BLANK)))
			.filter(s -> s != StateCreditApplication.PENDING)
			.switchIfEmpty(Mono.error(new InvalidStateCreditApplication(STATE_INVALID)));
	}
}
