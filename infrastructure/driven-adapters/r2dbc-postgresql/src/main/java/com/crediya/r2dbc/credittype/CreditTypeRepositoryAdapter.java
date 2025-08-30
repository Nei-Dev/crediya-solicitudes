package com.crediya.r2dbc.credittype;

import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.r2dbc.mappers.CreditTypeEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CreditTypeRepositoryAdapter implements CreditTypeRepository {
	
	private final CreditTypeReactiveRepository repository;
	
	@Override
	public Mono<CreditType> findById(Long idCreditType) {
		return repository.findById(idCreditType)
			.doOnSubscribe(subscription -> log.trace("Searching credit type in the database by id: {}", idCreditType))
			.doOnSuccess(data -> log.info("Credit type found in the database: {}", data.getName()))
			.map(CreditTypeEntityMapper.INSTANCE::toEntity);
	}
}
