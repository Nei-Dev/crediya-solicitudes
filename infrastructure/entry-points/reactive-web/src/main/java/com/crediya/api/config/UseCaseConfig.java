package com.crediya.api.config;

import com.crediya.model.auth.gateways.AuthService;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.usecase.createcreditapplication.CreateCreditApplicationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
	
	@Bean
	public CreateCreditApplicationUseCase createCreditApplicationUseCase(
		CreditApplicationRepository creditApplicationRepository,
		CreditTypeRepository creditTypeRepository,
		AuthService authService
	) {
		return new CreateCreditApplicationUseCase(creditApplicationRepository, creditTypeRepository, authService);
	}
}
