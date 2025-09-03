package com.crediya.api.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
	
	private static final String TITLE = "CrediYa Credit application API";
	private static final String DESCRIPTION = "API for managing credit applications";
	private static final String VERSION = "1.0.0";
	
	@Bean
	public OpenAPI creditApplicationOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title(TITLE)
				.version(VERSION)
				.description(DESCRIPTION));
	}
}

