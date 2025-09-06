package com.crediya.api;

import com.crediya.api.constants.Path;
import com.crediya.api.openapi.CreditApplicationDocApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
@RequiredArgsConstructor
public class CreditApplicationRouterRest {
    
    private final Path path;
 
    @Bean
    public RouterFunction<ServerResponse> routerFunction(CreditApplicationHandler creditApplicationHandler) {
        return route()
            .POST(
                path.getCreateApplication(),
                creditApplicationHandler::createApplication,
                CreditApplicationDocApi::createCreditApplicationDoc
            )
            .GET(
                path.getListCreditApplication(),
                creditApplicationHandler::getAllApplications,
                CreditApplicationDocApi::listCreditApplicationDoc
            )
            .build();
    }
}
