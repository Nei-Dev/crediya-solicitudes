package com.crediya.api.mapper;

import com.crediya.api.config.dto.output.creditapplication.CreditApplicationResponse;
import com.crediya.model.creditapplication.CreditApplication;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CreditApplicationResponseMapper {
	
	CreditApplicationResponseMapper INSTANCE = Mappers.getMapper(CreditApplicationResponseMapper.class);
	
	CreditApplicationResponse toResponse(CreditApplication entity);
}
