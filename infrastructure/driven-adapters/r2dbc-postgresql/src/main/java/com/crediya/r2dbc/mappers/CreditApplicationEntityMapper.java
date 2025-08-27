package com.crediya.r2dbc.mappers;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.r2dbc.entities.CreditApplicationData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CreditApplicationEntityMapper {
	
	CreditApplicationEntityMapper INSTANCE = Mappers.getMapper(CreditApplicationEntityMapper.class);
	
	CreditApplication toEntity(CreditApplicationData data);
	
	CreditApplicationData toData(CreditApplication entity);
}
