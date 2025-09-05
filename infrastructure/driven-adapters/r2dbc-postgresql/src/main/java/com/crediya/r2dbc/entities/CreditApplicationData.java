package com.crediya.r2dbc.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

import static com.crediya.r2dbc.entities.CreditApplicationColumns.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "application")
public class CreditApplicationData {
	
	@Id
	@Column(ID_APPLICATION)
	private Long idApplication;
	
	@Column(ID_CREDIT_TYPE)
	private Long idCreditType;
	
	@Column(ID_STATE)
	private Long idState;
	
	@Column(AMOUNT)
	private BigDecimal amount;
	
	@Column(TERM)
	private Integer term;
	
	@Column(EMAIL)
	private String email;
	
	@Column(CLIENT_NAME)
	private String clientName;
	
	@Column(CLIENT_SALARY_BASE)
	private BigDecimal clientSalaryBase;
	
}
