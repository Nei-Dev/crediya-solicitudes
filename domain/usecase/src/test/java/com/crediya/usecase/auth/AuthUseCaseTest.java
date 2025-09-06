package com.crediya.usecase.auth;

import com.crediya.model.auth.User;
import com.crediya.model.auth.gateways.AuthService;
import com.crediya.model.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.crediya.model.constants.CreateCreditApplicationErrorMessage.USER_NOT_FOUND;
import static com.crediya.model.constants.CreateCreditApplicationErrorMessage.USER_NOT_MATCH;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @InjectMocks
    private AuthUseCase authUseCase;

    @Mock
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();
    }

    @Test
    void getUserByToken_validToken_userFound() {
        String token = "valid-token";
        when(authService.findUserByToken(token)).thenReturn(Mono.just(user));

        StepVerifier.create(authUseCase.getUserByToken(token))
                .expectNextMatches(u -> u.getId().equals(user.getId()) && u.getEmail().equals(user.getEmail()))
                .verifyComplete();
    }

    @Test
    void getUserByToken_nullToken_shouldThrowBusinessException() {
        StepVerifier.create(authUseCase.getUserByToken(null))
                .expectErrorMatches(e -> e instanceof BusinessException && e.getMessage().equals(USER_NOT_MATCH))
                .verify();
    }

    @Test
    void getUserByToken_emptyToken_shouldThrowBusinessException() {
        StepVerifier.create(authUseCase.getUserByToken("   "))
                .expectErrorMatches(e -> e instanceof BusinessException && e.getMessage().equals(USER_NOT_MATCH))
                .verify();
    }

    @Test
    void getUserByToken_tokenNotFound_shouldThrowBusinessException() {
        String token = "not-found-token";
        when(authService.findUserByToken(token)).thenReturn(Mono.empty());

        StepVerifier.create(authUseCase.getUserByToken(token))
                .expectErrorMatches(e -> e instanceof BusinessException && e.getMessage().equals(USER_NOT_FOUND))
                .verify();
    }
}
