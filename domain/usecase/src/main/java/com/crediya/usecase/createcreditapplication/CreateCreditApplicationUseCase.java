package com.crediya.usecase.createcreditapplication;

import com.crediya.model.auth.gateways.AuthService;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.ports.ICreateCreditApplicationUseCase;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.credittype.CreditTypeNotFoundException;
import com.crediya.model.exceptions.credittype.InvalidCreditTypeException;
import com.crediya.model.exceptions.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.crediya.model.constants.ErrorMessage.*;
import static com.crediya.model.constants.Regex.EMAIL;
import static com.crediya.model.constants.Regex.IDENTIFICATION;
import static com.crediya.model.creditapplication.StateCreditApplication.PENDING;

@RequiredArgsConstructor
public class CreateCreditApplicationUseCase implements ICreateCreditApplicationUseCase {

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditTypeRepository creditTypeRepository;
    private final AuthService authService;
    
    @Override
    public Mono<CreditApplication> execute(CreditApplication creditApplication) {
        return Mono.defer(() -> {
                if (creditApplication == null) {
                    return Mono.error(new InvalidCreditApplicationException(NULL_CREDIT_APPLICATION));
                }
                return Mono.just(creditApplication);
            })
            .flatMap(this::validateAmountRequested)
            .flatMap(this::validateTermInMonths)
            .flatMap(this::validateIdCreditType)
            .flatMap(this::validateEmail)
            .flatMap(this::validateCreditType)
            .flatMap(this::validateIdentification)
            .flatMap(this::setPendingState)
            .flatMap(creditApplicationRepository::createApplication);
    }

    private Mono<CreditApplication> validateAmountRequested(CreditApplication creditApplication) {
        if (creditApplication.getAmount() == null || creditApplication.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidCreditApplicationException(INVALID_AMOUNT_REQUESTED));
        }
        return Mono.just(creditApplication);
    }

    private Mono<CreditApplication> validateTermInMonths(CreditApplication creditApplication) {
        if (creditApplication.getTerm() == null || creditApplication.getTerm() <= 0) {
            return Mono.error(new InvalidCreditApplicationException(INVALID_TERM_IN_MONTHS));
        }
        return Mono.just(creditApplication);
    }
	
	private Mono<CreditApplication> validateIdCreditType(CreditApplication creditApplication) {
		if (creditApplication.getIdCreditType() == null || creditApplication.getIdCreditType() <= 0) {
			return Mono.error(new InvalidCreditTypeException(INVALID_ID_CREDIT_TYPE));
		}
		return Mono.just(creditApplication);
	}

    private Mono<CreditApplication> validateEmail(CreditApplication creditApplication) {
        if (creditApplication.getEmail() == null || creditApplication.getEmail().trim().isEmpty() || !creditApplication.getEmail().matches(EMAIL)) {
            return Mono.error(new InvalidCreditApplicationException(INVALID_EMAIL));
        }
        
        return Mono.just(creditApplication);
    }
    
    private Mono<CreditApplication> validateIdentification(CreditApplication creditApplication) {
        if (creditApplication.getIdentification() == null || creditApplication.getIdentification().trim().isEmpty() || !creditApplication.getIdentification().matches(IDENTIFICATION)) {
            return Mono.error(new InvalidCreditApplicationException(INVALID_IDENTIFICATION));
        }
        return authService.findUserByIdentificationNumber(creditApplication.getIdentification())
            .switchIfEmpty(Mono.error(new UserNotFoundException(USER_NOT_FOUND)))
            .filter(user -> user.getEmail() != null && user.getEmail().equals(creditApplication.getEmail()))
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(USER_NOT_MATCH)))
            .thenReturn(creditApplication);
    }
    
    private Mono<CreditApplication> validateCreditType(CreditApplication creditApplication) {
        return creditTypeRepository.findById(creditApplication.getIdCreditType())
            .switchIfEmpty(Mono.error(new CreditTypeNotFoundException(CREDIT_TYPE_NOT_FOUND)))
            .filter(creditType -> creditApplication.getAmount().compareTo(creditType.getMinimumAmount()) > 0
                && creditApplication.getAmount().compareTo(creditType.getMaximumAmount()) < 0)
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(AMOUNT_REQUESTED_OUT_OF_RANGE)))
            .thenReturn(creditApplication);
    }
    
    private Mono<CreditApplication> setPendingState(CreditApplication creditApplication) {
        creditApplication.setState(PENDING);
        return Mono.just(creditApplication);
    }
}
