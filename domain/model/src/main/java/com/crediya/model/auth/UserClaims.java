package com.crediya.model.auth;

public record UserClaims (
	Long id,
	String email,
	String identification,
	UserRole role
) {
}
