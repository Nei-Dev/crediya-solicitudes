package com.crediya.model.auth;

import lombok.Getter;

@Getter
public enum AuthClaims {
	
	USER_ID("userId"),
	IDENTIFICATION("identification"),
	ROLE("role");
	
	private final String value;
	
	AuthClaims(String value) {
		this.value = value;
	}
	
}
