package com.crediya.usecase.updatestatecreditapplication;

import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.gateways.MessageChangeStatusService;
import com.crediya.model.creditapplication.ports.IUpdateStateCreditApplicationUseCase;
import com.crediya.model.exceptions.creditapplication.CreditApplicationNotFoundException;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.statecreditapplication.InvalidStateCreditApplication;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.CREDIT_APPLICATION_NOT_FOUND;
import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.INVALID_ID_CREDIT_APPLICATION;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.STATE_CANNOT_BE_MODIFIED;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.STATE_INVALID;
import static com.crediya.model.creditapplication.StateCreditApplication.*;

@RequiredArgsConstructor
public class UpdateStateCreditApplicationUseCase implements IUpdateStateCreditApplicationUseCase {
	
	private final CreditApplicationRepository repository;
	private final MessageChangeStatusService messageChangeStatusService;
	
	@Override
	public Mono<Void> execute(Long idCreditApplication, StateCreditApplication newState) {
		return this.validateState(newState)
			.flatMap(validState -> this.validateIdCreditApplication(idCreditApplication)
				.flatMap(repository::findById)
				.switchIfEmpty(Mono.error(new CreditApplicationNotFoundException(CREDIT_APPLICATION_NOT_FOUND)))
				.flatMap(creditApplication -> {
					if (creditApplication.getState() == StateCreditApplication.APPROVED || creditApplication.getState() == StateCreditApplication.REJECTED) {
						return Mono.error(new InvalidStateCreditApplication(STATE_CANNOT_BE_MODIFIED));
					}
					StateCreditApplication previousState = creditApplication.getState();
					creditApplication.setState(validState);
					return repository.saveCreditApplication(creditApplication)
						.filter(ca -> List.of(APPROVED, REJECTED).contains(ca.getState()))
						.switchIfEmpty(Mono.empty())
						.flatMap(saved -> messageChangeStatusService.sendChangeStateCreditApplication(saved)
							.onErrorResume(ex -> {
									creditApplication.setState(previousState);
									return repository.saveCreditApplication(creditApplication)
										.then(Mono.error(ex));
								}
							)
						);
				})
			)
			.then();
	}
	
	private Mono<Long> validateIdCreditApplication(Long idCreditApplication) {
		return Mono.justOrEmpty(idCreditApplication)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_ID_CREDIT_APPLICATION)))
			.filter(id -> id > 0)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_ID_CREDIT_APPLICATION)));
	}
	
	private Mono<StateCreditApplication> validateState(StateCreditApplication state) {
		return Mono.justOrEmpty(state)
			.switchIfEmpty(Mono.error(new InvalidStateCreditApplication(STATE_INVALID)))
			.filter(s -> s != PENDING)
			.switchIfEmpty(Mono.error(new InvalidStateCreditApplication(STATE_INVALID)));
	}
}
