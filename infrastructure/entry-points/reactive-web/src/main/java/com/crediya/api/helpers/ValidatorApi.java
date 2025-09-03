package com.crediya.api.helpers;

import com.crediya.model.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ValidatorApi {
	
	private final Validator validator;
	
	public <T> Mono<T> validate(T object){
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(object, object.getClass().getName());
		
		validator.validate(object, errors);
		
		if (errors.hasErrors()) {
			String errorMessage = errors.getAllErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.distinct()
				.collect(Collectors.joining("; "));
			
			return Mono.error(new BusinessException(errorMessage));
		}
		
		return Mono.just(object);
	}
}
