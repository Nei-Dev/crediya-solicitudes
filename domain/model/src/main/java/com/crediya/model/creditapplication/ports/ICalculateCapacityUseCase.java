package com.crediya.model.creditapplication.ports;

import reactor.core.publisher.Mono;

public interface ICalculateCapacityUseCase {
	
	Mono<Void> execute(Long idCreditApplication);
	
}
