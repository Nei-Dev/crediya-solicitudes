package com.crediya.api.config;

import com.crediya.api.constants.Path;
import com.crediya.api.dto.output.ErrorResponse;
import com.crediya.api.filters.CorrelationWebFilter;
import com.crediya.api.helpers.DefaultResponseHelper;
import com.crediya.model.auth.UserRole;
import com.crediya.model.auth.gateways.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.crediya.api.constants.ErrorMessage.ACCESS_DENIED;
import static com.crediya.api.constants.ErrorMessage.UNAUTHORIZED;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityFilterConfig implements WebFluxConfigurer {
    
    private static final String BEARER = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String[] ALLOWED_PATHS_SWAGGER = {
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/webjars/**"
    };

    private final Path path;
    private final TokenService tokenService;
    private final DefaultResponseHelper defaultResponseHelper;

    @Bean
    public SecurityWebFilterChain filterChain(
        ServerHttpSecurity http,
        AuthenticationWebFilter jwtAuthFilter,
        CorrelationWebFilter correlationWebFilter
    ) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .authorizeExchange(authorize -> authorize
                .pathMatchers(ALLOWED_PATHS_SWAGGER).permitAll()
                .pathMatchers(POST, path.getCreateApplication()).hasRole(UserRole.CLIENT.name())
                .anyExchange().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(this::handleSecurityException)
                .accessDeniedHandler(this::handleAccessDenied)
            )
            .addFilterAt(correlationWebFilter, SecurityWebFiltersOrder.FIRST)
            .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }
    
    @Bean
    public AuthenticationWebFilter jwtAuthFilter(ReactiveAuthenticationManager jwtReactiveAuthenticationManager) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(jwtReactiveAuthenticationManager);
        
        filter.setServerAuthenticationConverter(exchange -> {
            String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith(BEARER)) {
                String token = header.substring(BEARER.length());
                return Mono.just(new UsernamePasswordAuthenticationToken(null, token));
            }
            return Mono.empty();
        });
        
        return filter;
        
    }
    
    @Bean
    public ReactiveAuthenticationManager jwtReactiveAuthenticationManager() {
        return authentication -> {
            String token = authentication.getCredentials().toString();
            return tokenService.validateToken(token)
                .map(userClaims -> {
                    List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(ROLE_PREFIX.concat(userClaims.role().name()))
                    );
                    return new UsernamePasswordAuthenticationToken(
                        userClaims, token, authorities
                    );
                });
        };
    }
    
    private Mono<Void> handleSecurityException(ServerWebExchange exchange, AuthenticationException ex) {
        ErrorResponse errorResponse = ErrorResponse.unauthorized(UNAUTHORIZED);
        return defaultResponseHelper.writeJsonResponse(exchange.getResponse(), errorResponse);
    }
    
    private Mono<Void> handleAccessDenied(ServerWebExchange exchange, AccessDeniedException ex) {
        ErrorResponse response = ErrorResponse.unauthorized(ACCESS_DENIED);
        return defaultResponseHelper.writeJsonResponse(exchange.getResponse(), response);
    }
}
