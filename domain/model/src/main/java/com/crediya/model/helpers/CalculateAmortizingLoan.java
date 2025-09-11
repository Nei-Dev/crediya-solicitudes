package com.crediya.model.helpers;

import com.crediya.model.creditapplication.Installment;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CalculateAmortizingLoan {
	
	private static final int SCALE = 2;
	private static final int MONTHS_IN_YEAR = 12;
	
	public BigDecimal calculateMonthlyPayment(BigDecimal amountLoan, BigDecimal annualInterestRate, int termInMonths) {
		if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
			return divide(amountLoan, BigDecimal.valueOf(termInMonths));
		}
		
		if (termInMonths <= 0 || amountLoan.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		
		BigDecimal monthlyInterestRate = divide(annualInterestRate, BigDecimal.valueOf((MONTHS_IN_YEAR * 100)));
		BigDecimal onePlusRPowerN = (BigDecimal.ONE.add(monthlyInterestRate)).pow(termInMonths);
		BigDecimal numerator = amountLoan.multiply(monthlyInterestRate).multiply(onePlusRPowerN);
		BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
		
		return divide(numerator, denominator);
	}
	
	public List<Installment> generatePaymentPlan(BigDecimal amountLoan, BigDecimal annualInterestRate, int termInMonths) {
		
		BigDecimal monthlyPayment = calculateMonthlyPayment(amountLoan, annualInterestRate, termInMonths);
		BigDecimal monthlyInterestRate = divide(annualInterestRate, BigDecimal.valueOf((MONTHS_IN_YEAR * 100)));
		
		BigDecimal remainingBalance = amountLoan;
		
		List<Installment> schedule = new ArrayList<>();
		
		for(int i = 1; i <= termInMonths; i++) {
			BigDecimal interestPayment = multiply(remainingBalance, monthlyInterestRate);
			BigDecimal principalPayment = subtract(monthlyPayment, interestPayment);
			remainingBalance = subtract(remainingBalance, principalPayment);
			
			// Adjust last payment to avoid rounding issues
			if (i == termInMonths && remainingBalance.compareTo(BigDecimal.ZERO) != 0) {
				principalPayment = add(principalPayment, remainingBalance);
				monthlyPayment = add(interestPayment, principalPayment);
				remainingBalance = BigDecimal.ZERO;
			}
			
			schedule.add(new Installment(
				i,
				interestPayment,
				principalPayment,
				monthlyPayment,
				remainingBalance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remainingBalance
			));
		}
		
		return schedule;
	}
	
	private BigDecimal add(BigDecimal a, BigDecimal b) {
		return a.add(b).setScale(SCALE, RoundingMode.HALF_UP);
	}
	
	private BigDecimal subtract(BigDecimal a, BigDecimal b) {
		return a.subtract(b).setScale(SCALE, RoundingMode.HALF_UP);
	}
	
	private BigDecimal multiply(BigDecimal a, BigDecimal b) {
		return a.multiply(b).setScale(SCALE, RoundingMode.HALF_UP);
	}
	
	private BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
		if (denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
		return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
	}
}
