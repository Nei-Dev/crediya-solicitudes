package com.crediya.r2dbc.projections;

import java.math.BigDecimal;

public record CreditApplicationDataDebt (
	Long id_credit_application,
	BigDecimal amount,
	Integer term,
	BigDecimal interest_rate,
	BigDecimal salary_base
) {}
