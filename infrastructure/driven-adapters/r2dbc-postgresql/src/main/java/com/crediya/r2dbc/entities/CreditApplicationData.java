package com.crediya.r2dbc.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "solicitud")
public class CreditApplicationData {
	
	@Id
	@Column("id_solicitud")
	private Long idApplication;
	
	@Column("id_tipo_prestamo")
	private Long idCreditType;
	
	@Column("id_estado")
	private Long idState;
	
	@Column("monto")
	private String amount;
	
	@Column("plazo")
	private Integer term;
	
	@Column("email")
	private String email;
	
}
