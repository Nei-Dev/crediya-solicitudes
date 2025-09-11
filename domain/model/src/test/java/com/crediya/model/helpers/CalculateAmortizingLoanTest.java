package com.crediya.model.helpers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateAmortizingLoanTest {

    @Test
    void shouldReturnCorrectValue_whenInterestRateIsZero() {
        BigDecimal amount = new BigDecimal(1200);
        BigDecimal interest = BigDecimal.ZERO;
        int term = 12;
        
		BigDecimal result = CalculateAmortizingLoan.calculateMonthlyPayment(amount, interest, term);
        
        assertThat(result).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldReturnZero_whenTermIsZero() {
        BigDecimal amount = new BigDecimal(1200);
        BigDecimal interest = BigDecimal.ZERO;
        int term = 0;
        
        BigDecimal result = CalculateAmortizingLoan.calculateMonthlyPayment(amount, interest, term);
        
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnCorrectValue_whenInterestRateIsPositive() {
        BigDecimal amount = new BigDecimal(10_000);
        BigDecimal interest = new BigDecimal(12);
        int term = 12;
        
        BigDecimal result = CalculateAmortizingLoan.calculateMonthlyPayment(amount, interest, term);
        
        assertThat(result).isEqualByComparingTo(new BigDecimal("888.49"));
    }

    @Test
    void shouldReturnZero_whenAmountIsZero() {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interest = new BigDecimal(10);
        int term = 12;
        
        BigDecimal result = CalculateAmortizingLoan.calculateMonthlyPayment(amount, interest, term);
        
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZero_whenDenominatorIsZero() {
        BigDecimal amount = new BigDecimal(1000);
        BigDecimal interest = BigDecimal.ZERO;
        int term = 0;
        
        BigDecimal result = CalculateAmortizingLoan.calculateMonthlyPayment(amount, interest, term);
        
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZero_whenInterestRateIsPositiveAndTermIsZero() {
        BigDecimal amount = new BigDecimal(1000);
        BigDecimal interest = new BigDecimal(10);
        int term = 0;

        BigDecimal result = CalculateAmortizingLoan.calculateMonthlyPayment(amount, interest, term);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldHandleNegativeInterestRate() {
        BigDecimal amount = new BigDecimal(1000);
        BigDecimal interest = new BigDecimal(-5);
        int term = 12;

        BigDecimal result = CalculateAmortizingLoan.calculateMonthlyPayment(amount, interest, term);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldHandleNegativeTerm() {
        BigDecimal amount = new BigDecimal(1000);
        BigDecimal interest = new BigDecimal(10);
        int term = -12;

        BigDecimal result = CalculateAmortizingLoan.calculateMonthlyPayment(amount, interest, term);

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }
}
