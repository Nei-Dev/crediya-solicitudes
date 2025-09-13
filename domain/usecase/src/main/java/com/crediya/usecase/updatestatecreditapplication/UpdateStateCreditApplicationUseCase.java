package com.crediya.usecase.updatestatecreditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.Installment;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.gateways.MessageApprovedCreditService;
import com.crediya.model.creditapplication.gateways.MessageChangeStatusService;
import com.crediya.model.creditapplication.ports.IUpdateStateCreditApplicationUseCase;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.exceptions.creditapplication.CreditApplicationNotFoundException;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.credittype.CreditTypeNotFoundException;
import com.crediya.model.exceptions.statecreditapplication.InvalidStateCreditApplication;
import com.crediya.model.helpers.CalculateAmortizingLoan;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.*;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.STATE_CANNOT_BE_MODIFIED;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.STATE_INVALID;
import static com.crediya.model.creditapplication.StateCreditApplication.*;

@RequiredArgsConstructor
public class UpdateStateCreditApplicationUseCase implements IUpdateStateCreditApplicationUseCase {
	
	private final CreditApplicationRepository creditApplicationRepository;
	private final CreditTypeRepository creditTypeRepository;
	private final MessageChangeStatusService messageChangeStatusService;
	private final MessageApprovedCreditService messageApprovedCreditService;
	
	@Override
	public Mono<Void> execute(Long idCreditApplication, StateCreditApplication newState) {
		return Mono.zip(validateIdCreditApplication(idCreditApplication), validateState(newState))
			.flatMap(tuple -> updateState(tuple.getT1(), tuple.getT2()))
			.then();
	}
	
	private Mono<Void> updateState(Long id, StateCreditApplication newState) {
		return creditApplicationRepository.findById(id)
			.switchIfEmpty(Mono.error(new CreditApplicationNotFoundException(CREDIT_APPLICATION_NOT_FOUND)))
			.filter(this::isUpdatable)
			.switchIfEmpty(Mono.error(new InvalidStateCreditApplication(STATE_CANNOT_BE_MODIFIED)))
			.flatMap(creditApplication -> applyNewState(creditApplication, newState));
	}
	
	private boolean isUpdatable(CreditApplication creditApplication) {
		return creditApplication.getState() != APPROVED && creditApplication.getState() != REJECTED;
	}
	
	private Mono<Void> applyNewState(CreditApplication creditApplication, StateCreditApplication newState) {
		StateCreditApplication previousState = creditApplication.getState();
		creditApplication.setState(newState);
		
		return creditApplicationRepository.saveCreditApplication(creditApplication)
			.filter(ca -> isFinalState(ca.getState()))
			.flatMap(saved -> sendMessages(saved, previousState))
			.then();
	}
	
	private boolean isFinalState(StateCreditApplication state) {
		return List.of(APPROVED, REJECTED).contains(state);
	}
	
	private Mono<Void> sendMessages(CreditApplication creditApplication, StateCreditApplication previousState) {
		return creditTypeRepository.findById(creditApplication.getIdCreditType())
			.switchIfEmpty(Mono.error(new CreditTypeNotFoundException(CREDIT_TYPE_NOT_FOUND)))
			.flatMap(creditType -> creditApplication.getState().equals(APPROVED) ? Mono.just(CalculateAmortizingLoan.generatePaymentPlan(
				creditApplication.getAmount(),
				creditType.getInterestRate(),
				creditApplication.getTerm()
			)) : Mono.just(List.<Installment>of()))
			.flatMap(paymentPlan -> Mono.zip(
				messageChangeStatusService.sendChangeStateCreditApplication(creditApplication, paymentPlan),
				messageApprovedCreditService.sendApprovedCreditApplication(creditApplication)
			))
			.then()
			.onErrorResume(ex -> rollbackState(creditApplication, previousState, ex));
	}
	
	private Mono<Void> rollbackState(CreditApplication creditApplication, StateCreditApplication previousState, Throwable ex) {
		creditApplication.setState(previousState);
		return creditApplicationRepository.saveCreditApplication(creditApplication).then(Mono.error(ex));
	}
	
	private Mono<Long> validateIdCreditApplication(Long idCreditApplication) {
		return Mono.justOrEmpty(idCreditApplication)
			.filter(id -> id > 0)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_ID_CREDIT_APPLICATION)));
	}
	
	private Mono<StateCreditApplication> validateState(StateCreditApplication state) {
		return Mono.justOrEmpty(state)
			.filter(s -> s != PENDING)
			.switchIfEmpty(Mono.error(new InvalidStateCreditApplication(STATE_INVALID)));
	}
}
