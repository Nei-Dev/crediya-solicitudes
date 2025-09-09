package com.crediya.model.creditapplication;

import java.math.BigDecimal;

public record DebtCapacityCredit (

	BigDecimal totalIncome,
	BigDecimal monthlyNewLoanPayment,
	BigDecimal totalMonthlyDebtPayments

){}
