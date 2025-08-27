package com.crediya.model.creditapplication.gateways;

import com.crediya.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {
	
	Mono<CreditApplication> createApplication(CreditApplication creditApplication);
	
}
