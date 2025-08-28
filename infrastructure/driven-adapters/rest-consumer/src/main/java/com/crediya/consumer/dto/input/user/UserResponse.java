package com.crediya.consumer.dto.input.user;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserResponse(
	Long id,
	String fullname,
	String email,
	String address,
	String phone,
	LocalDate birthDate,
	BigDecimal salaryBase,
	String identification
) {}
