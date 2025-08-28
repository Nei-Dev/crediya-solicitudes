package com.crediya.model.creditapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreditApplication {
	
	private Long id;
	private BigDecimal amount;
	private Integer term;
	private String email;
	private String identification;
	private Long idCreditType;
	private StateCreditApplication state;
	
}
