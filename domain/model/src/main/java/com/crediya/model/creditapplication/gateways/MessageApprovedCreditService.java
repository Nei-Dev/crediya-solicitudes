package com.crediya.model.creditapplication.gateways;

import com.crediya.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface MessageApprovedCreditService {
	
	Mono<String> sendApprovedCreditApplication(CreditApplication creditApplication);
	
}
