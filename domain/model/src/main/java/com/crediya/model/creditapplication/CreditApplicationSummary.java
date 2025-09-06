package com.crediya.model.creditapplication;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreditApplicationSummary {
	
	private BigDecimal amount;
	private Integer term;
	private String email;
	private String clientName;
	private String creditType;
	private BigDecimal interestRate;
	private String stateApplication;
	private BigDecimal salaryBase;
	private BigDecimal monthlyAmount;
	
}
