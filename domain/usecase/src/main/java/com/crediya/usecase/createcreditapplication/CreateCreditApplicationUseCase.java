package com.crediya.usecase.createcreditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.DebtCapacityCredit;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.gateways.MessageDebtCapacityService;
import com.crediya.model.creditapplication.ports.ICreateCreditApplicationUseCase;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.credittype.CreditTypeNotFoundException;
import com.crediya.model.exceptions.credittype.InvalidCreditTypeException;
import com.crediya.model.helpers.CalculateAmortizingLoan;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.Objects;

import static com.crediya.model.constants.CreateCreditApplicationErrorMessage.*;
import static com.crediya.model.constants.Regex.EMAIL;
import static com.crediya.model.constants.Regex.IDENTIFICATION;
import static com.crediya.model.creditapplication.StateCreditApplication.PENDING;

@RequiredArgsConstructor
public class CreateCreditApplicationUseCase implements ICreateCreditApplicationUseCase {

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditTypeRepository creditTypeRepository;
    private final MessageDebtCapacityService messageDebtCapacityService;
    
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
            .flatMap(this::validateIdentification)
            .flatMap(this::validateCreditType)
            .flatMap(this::setPendingState)
            .flatMap(creditApplicationRepository::saveCreditApplication)
            .doOnSuccess(this::sendMessageDebtCapacity);
    }

    private Mono<CreditApplication> validateAmountRequested(CreditApplication creditApplication) {
        return Mono.just(creditApplication)
            .filter(app -> app.getAmount() != null)
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_AMOUNT_REQUESTED)))
            .filter(app -> app.getAmount().compareTo(BigDecimal.ZERO) > 0)
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_AMOUNT_REQUESTED)));
    }

    private Mono<CreditApplication> validateTermInMonths(CreditApplication creditApplication) {
        return Mono.just(creditApplication)
            .filter(app -> app.getTerm() != null)
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_TERM_IN_MONTHS)))
            .filter(app -> app.getTerm() > 0)
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_TERM_IN_MONTHS)));
    }
	
	private Mono<CreditApplication> validateIdCreditType(CreditApplication creditApplication) {
        return Mono.just(creditApplication)
            .filter(app -> Objects.nonNull(app.getIdCreditType()))
            .switchIfEmpty(Mono.error(new InvalidCreditTypeException(INVALID_ID_CREDIT_TYPE)))
            .filter(app -> app.getIdCreditType() > 0)
            .switchIfEmpty(Mono.error(new InvalidCreditTypeException(INVALID_ID_CREDIT_TYPE)));
	}

    private Mono<CreditApplication> validateEmail(CreditApplication creditApplication) {
        return Mono.just(creditApplication)
            .filter(app -> app.getEmail() != null)
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_EMAIL)))
            .filter(app -> !app.getEmail().trim().isEmpty())
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_EMAIL)))
            .filter(app -> app.getEmail().matches(EMAIL))
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_EMAIL)));
    }
    
    private Mono<CreditApplication> validateIdentification(CreditApplication creditApplication) {
        return Mono.just(creditApplication)
            .filter(app -> Objects.nonNull(app.getIdentification()))
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_IDENTIFICATION)))
            .filter(app -> !app.getIdentification().trim().isEmpty() && app.getIdentification().matches(IDENTIFICATION))
            .switchIfEmpty(Mono.error(new InvalidCreditApplicationException(INVALID_IDENTIFICATION)));
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
    
    private void sendMessageDebtCapacity(CreditApplication creditApplication) {
        creditApplicationRepository.findTotalMonthlyDebt(creditApplication.getEmail())
            .flatMap(monthlyDebt -> creditTypeRepository.findById(creditApplication.getIdCreditType())
                .flatMap(ct -> messageDebtCapacityService.sendChangeStateCreditApplication(new DebtCapacityCredit(
                    creditApplication.getClientSalaryBase(),
                    CalculateAmortizingLoan.apply(
                        creditApplication.getAmount(),
                        ct.getInterestRate(),
                        creditApplication.getTerm()
                    ),
                    monthlyDebt
                )))
            )
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }
}
