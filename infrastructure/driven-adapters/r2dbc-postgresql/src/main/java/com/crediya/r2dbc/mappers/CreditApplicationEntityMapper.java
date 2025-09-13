package com.crediya.r2dbc.mappers;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.r2dbc.creditapplication.CreditApplicationData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CreditApplicationEntityMapper {
	
	CreditApplicationEntityMapper INSTANCE = Mappers.getMapper(CreditApplicationEntityMapper.class);
	
	@Mapping(target = "id", source = "idApplication")
	CreditApplication toEntity(CreditApplicationData data);
	
	@Mapping(target = "idApplication", source = "id")
	CreditApplicationData toData(CreditApplication entity);
}
