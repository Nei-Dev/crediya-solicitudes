package com.crediya.model.projection;

import java.math.BigDecimal;

public interface CreditApplicationProjection {
	
	BigDecimal getAmount();
	Integer getTerm();
	String getEmail();
	String getClientName();
	String getCreditType();
	BigDecimal getInterestRate();
	String getStateApplication();
	BigDecimal getSalaryBase();
	BigDecimal getMonthlyAccumulatedApproved();
	
}
