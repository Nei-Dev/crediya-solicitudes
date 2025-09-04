package com.crediya.consumer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "adapter.restconsumer.path")
public class Path {
	
	private String me;
	
}
