package com.crediya.r2dbc.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "credit_type")
public class CreditTypeData {
	
	@Id
	@Column("id_credit_type")
	private Long id;
	
	@Column("name")
	private String name;
	
	@Column("minimum_amount")
	private BigDecimal minimumAmount;
	
	@Column("maximum_amount")
	private BigDecimal maximumAmount;
	
	@Column("interest_rate")
	private BigDecimal interestRate;
	
	@Column("auto_validation")
	private Boolean autoValidation;
}
