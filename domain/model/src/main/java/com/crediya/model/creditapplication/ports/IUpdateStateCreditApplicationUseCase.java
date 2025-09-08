package com.crediya.model.creditapplication.ports;

import com.crediya.model.creditapplication.StateCreditApplication;
import reactor.core.publisher.Mono;

public interface IUpdateStateCreditApplicationUseCase {
	
	Mono<Void> execute(Long idCreditApplication, StateCreditApplication state);
	
}
