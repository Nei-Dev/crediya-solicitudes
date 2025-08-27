package com.crediya.model.credittype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreditType {

	private Long idCreditType;
	private String name;
	private BigDecimal minimumAmount;
	private BigDecimal maximumAmount;
	private BigDecimal interestRate;
	private Boolean autoValidation;
	
}
