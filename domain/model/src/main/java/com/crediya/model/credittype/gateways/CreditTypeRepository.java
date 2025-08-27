package com.crediya.model.credittype.gateways;

import reactor.core.publisher.Mono;

public interface CreditTypeRepository {
	
	Mono<Boolean> existsByIdCreditType(Long idCreditType);
	
}
