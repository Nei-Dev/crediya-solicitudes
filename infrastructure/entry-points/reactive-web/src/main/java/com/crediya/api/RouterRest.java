package com.crediya.api;

import com.crediya.api.config.swagger.ApiDocHelper;
import com.crediya.api.config.swagger.CreditApplicationApiConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.crediya.api.constants.Path.APPLICATION_PATH;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
public class RouterRest {
 
    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route()
            .POST(APPLICATION_PATH,
                handler::createApplication,
                builder -> ApiDocHelper.commonErrorResponse(
                    CreditApplicationApiConfig.createCreditApplicationDoc(builder)
                )
            )
            .build();
    }
    
}
