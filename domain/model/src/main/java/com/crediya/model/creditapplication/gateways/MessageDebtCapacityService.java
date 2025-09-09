package com.crediya.model.creditapplication.gateways;

import com.crediya.model.creditapplication.DebtCapacityCredit;
import reactor.core.publisher.Mono;

public interface MessageDebtCapacityService {
	
	Mono<String> sendChangeStateCreditApplication(DebtCapacityCredit debtCapacityCredit);
	
}
