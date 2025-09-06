package com.crediya.consumer;

import com.crediya.consumer.config.Path;
import com.crediya.consumer.dto.input.user.UserResponse;
import com.crediya.consumer.mapper.UserEntityMapper;
import com.crediya.model.auth.User;
import com.crediya.model.auth.gateways.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthRestConsumer implements AuthService {
    
    private final WebClient client;
    private final Path authPath;
    
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    
    @Override
    public Mono<User> findUserByToken(String token) {
        return client
            .get()
            .uri(authPath.getMe())
            .header(org.springframework.http.HttpHeaders.AUTHORIZATION, BEARER_TOKEN_PREFIX.concat(token))
            .retrieve()
            .bodyToMono(UserResponse.class)
            .doOnSubscribe(subscription -> log.trace("Starting call to Auth Service to find user by token"))
            .doOnNext(response -> log.debug("Received response from Auth Service for token: {}", response))
            .map(UserEntityMapper.INSTANCE::toEntity)
            .onErrorResume(throwable -> throwable instanceof HttpClientErrorException.NotFound, throwable -> Mono.empty())
            .onErrorResume(
                WebClientResponseException.class::isInstance, throwable -> {
                    log.error("Error occurred while calling Auth Service: {}", throwable.getMessage());
                    return Mono.empty();
                }
            )
            .onErrorResume(
                WebClientRequestException.class::isInstance, throwable -> {
                    log.error("Auth Service is unreachable: {}", throwable.getMessage());
                    return Mono.empty();
                }
            );
    }

}