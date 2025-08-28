package com.crediya.api.mapper;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.model.creditapplication.CreditApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CreditApplicationEntityMapper {
	
	CreditApplicationEntityMapper INSTANCE = Mappers.getMapper(CreditApplicationEntityMapper.class);
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "state", ignore = true)
	CreditApplication toEntity(CreateCreditApplicationRequest request);
	
}
