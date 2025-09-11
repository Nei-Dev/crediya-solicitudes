package com.crediya.model.creditapplication.gateways;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.Installment;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MessageChangeStatusService {
	
	Mono<String> sendChangeStateCreditApplication(CreditApplication creditApplication, List<Installment> paymentPlan);
	
}
