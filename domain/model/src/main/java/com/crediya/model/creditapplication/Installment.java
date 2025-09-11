package com.crediya.model.creditapplication;

import java.math.BigDecimal;

public record Installment (
	int number,
	BigDecimal interestPayment,
	BigDecimal principalPayment,
	BigDecimal totalPayment,
	BigDecimal remainingBalance
) {}
