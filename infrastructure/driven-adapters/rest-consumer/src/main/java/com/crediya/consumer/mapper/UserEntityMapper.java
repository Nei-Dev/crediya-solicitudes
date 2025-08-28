package com.crediya.consumer.mapper;

import com.crediya.consumer.dto.input.user.UserResponse;
import com.crediya.model.auth.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserEntityMapper {
	
	UserEntityMapper INSTANCE = Mappers.getMapper(UserEntityMapper.class);
	
	User toEntity(UserResponse userResponse);
}
