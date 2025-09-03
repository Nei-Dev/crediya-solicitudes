package com.crediya.api.constants;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.paths.credit-application")
public class Path {
	
	private String createApplication;
	private String listCreditApplication;
	
}
