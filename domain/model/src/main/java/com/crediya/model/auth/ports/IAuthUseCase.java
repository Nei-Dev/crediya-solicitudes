package com.crediya.model.auth.ports;

import com.crediya.model.auth.User;
import reactor.core.publisher.Mono;

public interface IAuthUseCase {
	
	Mono<User> getUserByToken(String token);
	
}
