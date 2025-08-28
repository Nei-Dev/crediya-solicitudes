package com.crediya.consumer;

import com.crediya.consumer.dto.input.user.UserResponse;
import com.crediya.consumer.mapper.UserEntityMapper;
import com.crediya.model.auth.User;
import com.crediya.model.auth.gateways.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static com.crediya.consumer.constants.Path.SEARCH_BY_IDENTIFICATION_PATH;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthRestConsumer implements AuthService {
    
    private final WebClient client;
    
    @Override
    public Mono<User> findUserByIdentificationNumber(String identification) {
        return client
            .get()
            .uri(SEARCH_BY_IDENTIFICATION_PATH.concat(identification))
            .retrieve()
            .bodyToMono(UserResponse.class)
            .doOnSubscribe(subscription -> log.trace("Starting call to Auth Service to find user by identification number: {}", identification))
            .doOnNext(response -> log.debug("Received response from Auth Service for identification number {}: {}", identification, response))
            .map(UserEntityMapper.INSTANCE::toEntity)
            .onErrorResume(throwable -> throwable instanceof HttpClientErrorException.NotFound, throwable -> Mono.empty())
            .onErrorResume(WebClientResponseException.class::isInstance, throwable -> {
                log.error("Error occurred while calling Auth Service: {}", throwable.getMessage());
                return Mono.empty();
            });
    }

}