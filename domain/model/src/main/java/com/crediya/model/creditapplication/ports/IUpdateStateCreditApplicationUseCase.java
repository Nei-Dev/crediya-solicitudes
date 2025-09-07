package com.crediya.model.creditapplication.ports;

import reactor.core.publisher.Mono;

public interface IUpdateStateCreditApplicationUseCase {
	
	Mono<Void> execute(Long idCreditApplication, String state);
	
}
