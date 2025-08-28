package com.crediya.model.auth.gateways;

import com.crediya.model.auth.User;
import reactor.core.publisher.Mono;

public interface AuthService {
	
	Mono<User> findUserByIdentificationNumber(String email);
	
}
