package com.crediya.usecase.calculatecapacity;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.DebtCapacityCredit;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.gateways.MessageDebtCapacityService;
import com.crediya.model.creditapplication.ports.ICalculateCapacityUseCase;
import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.exceptions.TechnicalException;
import com.crediya.model.exceptions.creditapplication.CreditApplicationNotFoundException;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.statecreditapplication.InvalidStateCreditApplication;
import com.crediya.model.helpers.CalculateAmortizingLoan;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.crediya.model.constants.CalculateCapacityCreditApplicationErrorMessage.FETCHING_DATA_ERROR;
import static com.crediya.model.constants.CalculateCapacityCreditApplicationErrorMessage.STATE_MUST_BE_PENDING;
import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.CREDIT_APPLICATION_NOT_FOUND;
import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.INVALID_ID_CREDIT_APPLICATION;
import static com.crediya.model.creditapplication.StateCreditApplication.PENDING;

@RequiredArgsConstructor
public class CalculateCapacityUseCase implements ICalculateCapacityUseCase {
	
	private final CreditApplicationRepository creditApplicationRepository;
	private final CreditTypeRepository creditTypeRepository;
	private final MessageDebtCapacityService messageDebtCapacityService;
	
	@Override
	public Mono<Void> execute(Long idCreditApplication) {
		return Mono.justOrEmpty(idCreditApplication)
			.switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_ID_CREDIT_APPLICATION)))
			.flatMap(creditApplicationRepository::findById)
			.switchIfEmpty(Mono.error(new CreditApplicationNotFoundException(CREDIT_APPLICATION_NOT_FOUND)))
			.filter(ca -> ca.getState().equals(PENDING))
			.switchIfEmpty(Mono.error(new InvalidStateCreditApplication(STATE_MUST_BE_PENDING)))
			.flatMap(this::sendMessage)
			.then();
	}
	
	private Mono<String> sendMessage(CreditApplication creditApplication) {
		return Mono.zip(
				creditApplicationRepository.findTotalMonthlyDebt(creditApplication.getEmail()),
				creditTypeRepository.findById(creditApplication.getIdCreditType())
			)
			.onErrorMap(e -> new TechnicalException(FETCHING_DATA_ERROR))
			.flatMap(tuple -> {
				BigDecimal monthlyDebt = tuple.getT1();
				CreditType ct = tuple.getT2();
				return messageDebtCapacityService.sendCalculateDebtCapacity(new DebtCapacityCredit(
					creditApplication.getId(),
					creditApplication.getClientSalaryBase(),
					creditApplication.getAmount(),
					CalculateAmortizingLoan.calculateMonthlyPayment(
						creditApplication.getAmount(),
						ct.getInterestRate(),
						creditApplication.getTerm()
					),
					monthlyDebt
				));
			});
	}
	
}
