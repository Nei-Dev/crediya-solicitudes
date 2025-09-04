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
@Table(name = "application")
public class CreditApplicationData {
	
	@Id
	@Column("id_application")
	private Long idApplication;
	
	@Column("id_credit_type")
	private Long idCreditType;
	
	@Column("id_state")
	private Long idState;
	
	@Column("amount")
	private BigDecimal amount;
	
	@Column("term")
	private Integer term;
	
	@Column("email")
	private String email;
	
	@Column("client_name")
	private String clientName;
	
	@Column("client_salary_base")
	private BigDecimal clientSalaryBase;
	
}
