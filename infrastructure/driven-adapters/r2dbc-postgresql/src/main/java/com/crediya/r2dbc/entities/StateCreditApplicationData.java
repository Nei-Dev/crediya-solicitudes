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
@Table(name = "estado")
public class StateCreditApplicationData {

	@Id
	@Column("id_estado")
	private Long id;
	
	@Column("nombre")
	private String name;
	
	@Column("descripcion")
	private String description;
	
}
