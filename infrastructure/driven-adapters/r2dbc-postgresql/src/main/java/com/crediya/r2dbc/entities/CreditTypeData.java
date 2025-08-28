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
@Table(name = "tipo_prestamo")
public class CreditTypeData {
	
	@Id
	@Column("id_tipo_prestamo")
	private Long id;
	
	@Column("nombre")
	private String name;
	
	@Column("monto_minimo")
	private BigDecimal minimumAmount;
	
	@Column("monto_maximo")
	private BigDecimal maximumAmount;
	
	@Column("tasa_interes")
	private BigDecimal interestRate;
	
	@Column("validacion_automatica")
	private Boolean autoValidation;
}
