package com.crediya.r2dbc.projections;

import java.math.BigDecimal;

public interface CreditApplicationDataDebt {
	
	String getIdCreditApplication();
	BigDecimal getAmount();
	Integer getTerm();
	BigDecimal getInterestRate();
	BigDecimal getSalaryBase();
	
}
