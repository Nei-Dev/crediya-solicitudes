package com.crediya.model.creditapplication;

import java.math.BigDecimal;

public record DebtCapacityCredit (

	Long idCreditApplication,
	BigDecimal salaryBase,
	BigDecimal amount,
	BigDecimal monthlyNewLoanPayment,
	BigDecimal totalMonthlyDebtPayments

){}
