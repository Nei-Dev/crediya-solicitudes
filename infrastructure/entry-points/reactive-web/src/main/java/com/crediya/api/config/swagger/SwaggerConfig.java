package com.crediya.api.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.crediya.api.constants.swagger.DocApi.*;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI creditApplicationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(TITLE)
                        .version(VERSION)
                        .description(DESCRIPTION));
    }
}

