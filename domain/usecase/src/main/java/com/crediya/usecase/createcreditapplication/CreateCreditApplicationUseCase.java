package com.crediya.usecase.createcreditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.ports.ICreateCreditApplicationUseCase;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.exceptions.BusinessException;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.credittype.InvalidCreditTypeException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.crediya.model.constants.ErrorMessage.*;
import static com.crediya.model.constants.Regex.EMAIL;
import static com.crediya.model.creditapplication.StateCreditApplication.PENDING;

@RequiredArgsConstructor
public class CreateCreditApplicationUseCase implements ICreateCreditApplicationUseCase {
	
	private final CreditApplicationRepository creditApplicationRepository;
	private final CreditTypeRepository creditTypeRepository;
	
	@Override
	public Mono<CreditApplication> execute(CreditApplication creditApplication) {
		try {
			validateCreditApplication(creditApplication);
		} catch (BusinessException e) {
			return Mono.error(e);
		}
		
		creditApplication.setState(PENDING);
		
		return creditTypeRepository.existsByIdCreditType(creditApplication.getIdCreditType())
			.flatMap(exists -> {
				if (Boolean.FALSE.equals(exists)) {
					return Mono.error(new InvalidCreditTypeException(CREDIT_TYPE_NOT_FOUND));
				}
				return creditApplicationRepository.createApplication(creditApplication);
			});
	}
	
	private void validateCreditApplication(CreditApplication creditApplication) {
		if (creditApplication == null) {
			throw new InvalidCreditApplicationException(NULL_CREDIT_APPLICATION);
		}
		
		validateAmountRequested(creditApplication.getAmount());
		validateTermInMonths(creditApplication.getTerm());
		validateIdCreditType(creditApplication.getIdCreditType());
		validateEmail(creditApplication.getEmail());
	}
	
	private void validateAmountRequested(BigDecimal amountRequested) {
		if (amountRequested == null || amountRequested.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidCreditApplicationException(INVALID_AMOUNT_REQUESTED);
		}
	}
	
	private void validateTermInMonths(Integer termInMonths) {
		if (termInMonths == null || termInMonths <= 0) {
			throw new InvalidCreditApplicationException(INVALID_TERM_IN_MONTHS);
		}
	}
	
	private void validateIdCreditType(Long idCreditType) {
		if (idCreditType == null || idCreditType <= 0) {
			throw new InvalidCreditTypeException(INVALID_ID_CREDIT_TYPE);
		}
	}
	
	private void validateEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new InvalidCreditApplicationException(INVALID_EMAIL);
		}
		if(!email.matches(EMAIL)) {
			throw new InvalidCreditApplicationException(INVALID_EMAIL);
		}
	}
	
}
