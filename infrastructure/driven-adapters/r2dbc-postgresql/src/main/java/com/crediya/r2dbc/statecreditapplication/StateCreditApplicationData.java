package com.crediya.r2dbc.statecreditapplication;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "state")
public class StateCreditApplicationData {

	@Id
	@Column("id_state")
	private Long id;
	
	@Column("name")
	private String name;
	
	@Column("description")
	private String description;
	
}
