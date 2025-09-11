package com.crediya.usecase.createcreditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.gateways.MessageDebtCapacityService;
import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.credittype.CreditTypeNotFoundException;
import com.crediya.model.exceptions.credittype.InvalidCreditTypeException;
import com.crediya.model.helpers.CalculateAmortizingLoan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Objects;

import static com.crediya.model.constants.CreateCreditApplicationErrorMessage.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCreditApplicationUseCaseTest {

    @InjectMocks
    private CreateCreditApplicationUseCase useCase;

    @Mock
    private CreditApplicationRepository creditApplicationRepository;

    @Mock
    private CreditTypeRepository creditTypeRepository;
    
    @Mock
    private MessageDebtCapacityService messageDebtCapacityService;
    
    private CreditApplication creditApplication;
    
    private CreditType creditTypeRegistered;
    
    @BeforeEach
    void setUp() {
        creditApplication = CreditApplication
            .builder()
            .amount(BigDecimal.valueOf(1000))
            .term(12)
            .idCreditType(1L)
            .email("test@example.com")
            .identification("123456789")
            .build();
        
        creditTypeRegistered = CreditType.builder()
            .idCreditType(1L)
            .name("Personal Loan")
            .minimumAmount(BigDecimal.valueOf(500))
            .maximumAmount(BigDecimal.valueOf(5000))
            .build();
    }
    
    @Test
    void execute_validCreditApplication_shouldCreateApplication() {
        CreditApplication creditApplicationSaved = CreditApplication
            .builder()
            .id(1L)
            .amount(BigDecimal.valueOf(1000))
            .term(12)
            .idCreditType(1L)
            .email("test@example.com")
            .build();

        when(creditTypeRepository.findById(any(Long.class)))
                .thenReturn(Mono.just(creditTypeRegistered));
        when(creditApplicationRepository.saveCreditApplication(any(CreditApplication.class)))
                .thenReturn(Mono.just(creditApplicationSaved));

        StepVerifier.create(useCase.execute(creditApplication))
                .expectNextMatches(result -> Objects.nonNull(result.getId()) && result.getId().equals(creditApplicationSaved.getId())
                    && result.getAmount().equals(creditApplicationSaved.getAmount())
                    && result.getTerm().equals(creditApplicationSaved.getTerm())
                    && result.getIdCreditType().equals(creditApplicationSaved.getIdCreditType())
                    && result.getEmail().equals(creditApplicationSaved.getEmail()
                ))
                .verifyComplete();
    }

    @Test
    void execute_nullCreditApplication_shouldThrowException() {
        StepVerifier.create(useCase.execute(null))
            .expectError(InvalidCreditApplicationException.class)
            .verify();
    }

    @Test
    void execute_creditTypeNotFound_shouldThrowException() {
        creditApplication.setIdCreditType(10L);

        when(creditTypeRepository.findById(creditApplication.getIdCreditType()))
            .thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(creditApplication))
            .expectError(CreditTypeNotFoundException.class)
            .verify();
    }
    
    @Test
    void execute_invalidAmount_shouldThrowException() {
        creditApplication.setAmount(BigDecimal.valueOf(-1000));
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException
                && e.getMessage().equals(INVALID_AMOUNT_REQUESTED))
            .verify();
        
        creditApplication.setAmount(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException
                && e.getMessage().equals(INVALID_AMOUNT_REQUESTED))
            .verify();
    }
    
    @Test
    void execute_invalidTerm_shouldThrowException() {
        creditApplication.setTerm(0);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException
                && e.getMessage().equals(INVALID_TERM_IN_MONTHS))
            .verify();
        
        creditApplication.setTerm(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException
                && e.getMessage().equals(INVALID_TERM_IN_MONTHS))
            .verify();
    }
    
    @Test
    void execute_invalidIdCreditTypeOrEmail_shouldThrowException() {
        creditApplication.setIdCreditType(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditTypeException && e.getMessage().equals(INVALID_ID_CREDIT_TYPE))
            .verify();
        
        creditApplication.setIdCreditType(0L);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditTypeException && e.getMessage().equals(INVALID_ID_CREDIT_TYPE))
            .verify();
    }
    
    @Test
    void execute_invalidEmail_shouldThrowException() {
        creditApplication.setEmail(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_EMAIL))
            .verify();
        
        creditApplication.setEmail("");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_EMAIL))
            .verify();
        
        creditApplication.setEmail("invalid-email");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_EMAIL))
            .verify();
    }
    
    @Test
    void execute_outOfRangeAmount_shouldThrowException() {
        creditApplication.setAmount(BigDecimal.valueOf(10000));
        when(creditTypeRepository.findById(creditApplication.getIdCreditType()))
            .thenReturn(Mono.just(creditTypeRegistered));
        
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(AMOUNT_REQUESTED_OUT_OF_RANGE))
            .verify();
        
        creditApplication.setAmount(BigDecimal.valueOf(100));
        when(creditTypeRepository.findById(creditApplication.getIdCreditType()))
            .thenReturn(Mono.just(creditTypeRegistered));
        
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(AMOUNT_REQUESTED_OUT_OF_RANGE))
            .verify();
    }
    
    @Test
    void execute_invalidIdentification_shouldThrowException() {
        creditApplication.setIdentification(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_IDENTIFICATION))
            .verify();
        
        creditApplication.setIdentification("");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_IDENTIFICATION))
            .verify();
        
        creditApplication.setIdentification("invalid-id!");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_IDENTIFICATION))
            .verify();
    }
    
    @Test
    void execute_autoValidationCreditType_shouldSendDebtCapacityMessage() {
        CreditType autoValidationCreditType = CreditType.builder()
            .idCreditType(1L)
            .name("Personal Loan")
            .minimumAmount(BigDecimal.valueOf(500))
            .maximumAmount(BigDecimal.valueOf(5000))
            .interestRate(BigDecimal.valueOf(0.05))
            .autoValidation(true)
            .build();
        CreditApplication creditApplicationWithSalary = CreditApplication.builder()
            .amount(BigDecimal.valueOf(1000))
            .term(12)
            .idCreditType(1L)
            .email("test@example.com")
            .identification("123456789")
            .clientSalaryBase(BigDecimal.valueOf(2000))
            .build();
        when(creditTypeRepository.findById(any(Long.class)))
            .thenReturn(Mono.just(autoValidationCreditType));
        when(creditApplicationRepository.saveCreditApplication(any(CreditApplication.class)))
            .thenReturn(Mono.just(creditApplicationWithSalary));
        when(creditApplicationRepository.findTotalMonthlyDebt(creditApplicationWithSalary.getEmail()))
            .thenReturn(Mono.just(BigDecimal.valueOf(100)));
        when(messageDebtCapacityService.sendCalculateDebtCapacity(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(creditApplicationWithSalary))
            .expectNextMatches(result -> result.getAmount().equals(creditApplicationWithSalary.getAmount()))
            .verifyComplete();
        
        verify(messageDebtCapacityService, times(1))
            .sendCalculateDebtCapacity(any());
    }
    
    @Test
    void execute_amountEqualsMinOrMax_shouldThrowException() {
        creditApplication.setAmount(creditTypeRegistered.getMinimumAmount());
        when(creditTypeRepository.findById(creditApplication.getIdCreditType()))
            .thenReturn(Mono.just(creditTypeRegistered));
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(AMOUNT_REQUESTED_OUT_OF_RANGE))
            .verify();

        creditApplication.setAmount(creditTypeRegistered.getMaximumAmount());
        when(creditTypeRepository.findById(creditApplication.getIdCreditType()))
            .thenReturn(Mono.just(creditTypeRegistered));
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(AMOUNT_REQUESTED_OUT_OF_RANGE))
            .verify();
    }

    @Test
    void execute_negativeTerm_shouldThrowException() {
        creditApplication.setTerm(-5);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_TERM_IN_MONTHS))
            .verify();
    }

    @Test
    void execute_negativeIdCreditType_shouldThrowException() {
        creditApplication.setIdCreditType(-1L);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditTypeException && e.getMessage().equals(INVALID_ID_CREDIT_TYPE))
            .verify();
    }

    @Test
    void execute_emailWithSpaces_shouldThrowException() {
        creditApplication.setEmail("   ");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_EMAIL))
            .verify();
    }

    @Test
    void execute_identificationWithSpaces_shouldThrowException() {
        creditApplication.setIdentification("   ");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_IDENTIFICATION))
            .verify();
    }

    @Test
    void execute_autoValidationFalse_shouldNotSendDebtCapacityMessage() {
        CreditType noAutoValidationCreditType = CreditType.builder()
            .idCreditType(1L)
            .name("Personal Loan")
            .minimumAmount(BigDecimal.valueOf(500))
            .maximumAmount(BigDecimal.valueOf(5000))
            .interestRate(BigDecimal.valueOf(0.05))
            .autoValidation(false)
            .build();
        when(creditTypeRepository.findById(any(Long.class)))
            .thenReturn(Mono.just(noAutoValidationCreditType));
        when(creditApplicationRepository.saveCreditApplication(any(CreditApplication.class)))
            .thenReturn(Mono.just(creditApplication));
        StepVerifier.create(useCase.execute(creditApplication))
            .expectNextCount(1)
            .verifyComplete();
        verify(messageDebtCapacityService, never()).sendCalculateDebtCapacity(any());
    }

    @Test
    void execute_errorOnSave_shouldPropagateError() {
        when(creditTypeRepository.findById(any(Long.class)))
            .thenReturn(Mono.just(creditTypeRegistered));
        when(creditApplicationRepository.saveCreditApplication(any(CreditApplication.class)))
            .thenReturn(Mono.error(new RuntimeException("DB error")));
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("DB error"))
            .verify();
    }

    @Test
    void execute_errorOnSendDebtCapacityMessage_shouldNotAffectMainFlow() {
        CreditType autoValidationCreditType = CreditType.builder()
            .idCreditType(1L)
            .name("Personal Loan")
            .minimumAmount(BigDecimal.valueOf(500))
            .maximumAmount(BigDecimal.valueOf(5000))
            .interestRate(BigDecimal.valueOf(0.05))
            .autoValidation(true)
            .build();
        CreditApplication creditApplicationWithSalary = CreditApplication.builder()
            .amount(BigDecimal.valueOf(1000))
            .term(12)
            .idCreditType(1L)
            .email("test@example.com")
            .identification("123456789")
            .clientSalaryBase(BigDecimal.valueOf(2000))
            .build();
        when(creditTypeRepository.findById(any(Long.class)))
            .thenReturn(Mono.just(autoValidationCreditType));
        when(creditApplicationRepository.saveCreditApplication(any(CreditApplication.class)))
            .thenReturn(Mono.just(creditApplicationWithSalary));
        
        try (MockedStatic<CalculateAmortizingLoan> mocked = mockStatic(CalculateAmortizingLoan.class)) {
            mocked.when(() -> CalculateAmortizingLoan.apply(any(BigDecimal.class), eq(BigDecimal.ONE), eq(2)))
                .thenReturn(BigDecimal.valueOf(85.61));
            StepVerifier.create(useCase.execute(creditApplicationWithSalary))
                .expectNextMatches(result -> result.getAmount().equals(creditApplicationWithSalary.getAmount()))
                .verifyComplete();
        }
    }

    @Test
    void execute_shouldSetPendingState() {
        when(creditTypeRepository.findById(any(Long.class)))
            .thenReturn(Mono.just(creditTypeRegistered));
        when(creditApplicationRepository.saveCreditApplication(any(CreditApplication.class)))
            .thenAnswer(invocation -> {
                CreditApplication app = invocation.getArgument(0);
                return Mono.just(app);
            });
        StepVerifier.create(useCase.execute(creditApplication))
            .expectNextMatches(result -> result.getState() != null && result.getState().name().equals("PENDING"))
            .verifyComplete();
    }
}
