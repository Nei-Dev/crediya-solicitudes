package com.crediya.usecase.auth;

import com.crediya.model.auth.User;
import com.crediya.model.auth.gateways.AuthService;
import com.crediya.model.auth.ports.IAuthUseCase;
import com.crediya.model.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.crediya.model.constants.ErrorMessage.USER_NOT_FOUND;
import static com.crediya.model.constants.ErrorMessage.USER_NOT_MATCH;

@RequiredArgsConstructor
public class AuthUseCase implements IAuthUseCase {
	
	private final AuthService authService;
	
	@Override
	public Mono<User> getUserByToken(String token) {
		return Mono.justOrEmpty(token)
			.filter(Objects::nonNull)
			.filter(t -> !t.trim().isEmpty())
			.switchIfEmpty(Mono.error(new BusinessException(USER_NOT_MATCH)))
			.flatMap(authService::findUserByToken)
			.switchIfEmpty(Mono.error(new BusinessException(USER_NOT_FOUND)));
	}
}
