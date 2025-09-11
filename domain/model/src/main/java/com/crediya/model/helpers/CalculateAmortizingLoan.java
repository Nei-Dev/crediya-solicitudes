package com.crediya.model.helpers;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class CalculateAmortizingLoan {
	
	private static final int SCALE = 2;
	private static final int MONTHS_IN_YEAR = 12;
	
	public BigDecimal apply(BigDecimal amountLoan, BigDecimal annualInterestRate, int termInMonths) {
		if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
			return divide(amountLoan, BigDecimal.valueOf(termInMonths));
		}
		
		BigDecimal monthlyInterestRate = divide(annualInterestRate, BigDecimal.valueOf((MONTHS_IN_YEAR * 100)));
		BigDecimal onePlusRPowerN = (BigDecimal.ONE.add(monthlyInterestRate)).pow(termInMonths);
		BigDecimal numerator = amountLoan.multiply(monthlyInterestRate).multiply(onePlusRPowerN);
		BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
		
		return divide(numerator, denominator);
	}
	
	private BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
		if (denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
		return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
	}
}
