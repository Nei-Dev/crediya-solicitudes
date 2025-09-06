package com.crediya.api.config;

import com.crediya.model.auth.gateways.AuthService;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.usecase.auth.AuthUseCase;
import com.crediya.usecase.createcreditapplication.CreateCreditApplicationUseCase;
import com.crediya.usecase.getcreditapplication.GetCreditApplicationPaginatedUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
	
	@Bean
	public CreateCreditApplicationUseCase createCreditApplicationUseCase(
		CreditApplicationRepository creditApplicationRepository,
		CreditTypeRepository creditTypeRepository
	) {
		return new CreateCreditApplicationUseCase(creditApplicationRepository, creditTypeRepository);
	}
	
	@Bean
	public AuthUseCase authUseCase(
		AuthService authService
	) {
		return new AuthUseCase(authService);
	}
	
	@Bean
	public GetCreditApplicationPaginatedUseCase getCreditApplicationPaginatedUseCase(
		CreditApplicationRepository creditApplicationRepository
	) {
		return new GetCreditApplicationPaginatedUseCase(creditApplicationRepository);
	}
}
