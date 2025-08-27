package com.crediya.r2dbc.credittype;

import com.crediya.model.credittype.gateways.CreditTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CreditTypeRepositoryAdapter implements CreditTypeRepository {
	
	private final CreditTypeReactiveRepository repository;
	
	@Override
	public Mono<Boolean> existsByIdCreditType(Long idCreditType) {
		return repository.existsById(idCreditType);
	}
}
