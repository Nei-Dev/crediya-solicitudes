package com.crediya.model.auth;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	
	Long id;
	String fullname;
	String email;
	String address;
	String phone;
	LocalDate birthDate;
	BigDecimal salaryBase;
	String identification;
	
}
