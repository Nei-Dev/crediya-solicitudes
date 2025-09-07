package com.crediya.model.creditapplication.gateways;

import com.crediya.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface MessageService {
	
	Mono<String> sendChangeStateCreditApplication(CreditApplication creditApplication);
	
}
