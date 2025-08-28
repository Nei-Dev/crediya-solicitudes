package com.crediya.r2dbc.credittype;

import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.r2dbc.mappers.CreditTypeEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CreditTypeRepositoryAdapter implements CreditTypeRepository {
	
	private final CreditTypeReactiveRepository repository;
	
	@Override
	public Mono<CreditType> findById(Long idCreditType) {
		return repository.findById(idCreditType)
			.map(CreditTypeEntityMapper.INSTANCE::toEntity);
	}
}
