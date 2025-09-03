package com.crediya.model.auth.gateways;

import com.crediya.model.auth.UserClaims;
import reactor.core.publisher.Mono;

public interface TokenService {
	
	Mono<UserClaims> validateToken(String token);
	
}
