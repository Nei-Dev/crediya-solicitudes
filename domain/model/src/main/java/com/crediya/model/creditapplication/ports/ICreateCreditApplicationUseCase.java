package com.crediya.model.creditapplication.ports;

import com.crediya.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface ICreateCreditApplicationUseCase {
	
	Mono<CreditApplication> execute(CreditApplication creditApplication);
	
}
