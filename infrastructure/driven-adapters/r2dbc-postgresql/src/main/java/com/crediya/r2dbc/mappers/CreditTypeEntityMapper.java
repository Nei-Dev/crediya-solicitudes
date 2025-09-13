package com.crediya.r2dbc.mappers;

import com.crediya.model.credittype.CreditType;
import com.crediya.r2dbc.credittype.CreditTypeData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CreditTypeEntityMapper {
	
	CreditTypeEntityMapper INSTANCE = Mappers.getMapper(CreditTypeEntityMapper.class);
	
	CreditType toEntity(CreditTypeData creditTypeData);
	
}
