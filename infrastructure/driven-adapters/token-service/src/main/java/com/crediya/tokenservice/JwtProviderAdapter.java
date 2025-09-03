package com.crediya.tokenservice;

import com.crediya.model.auth.AuthClaims;
import com.crediya.model.auth.UserClaims;
import com.crediya.model.auth.UserRole;
import com.crediya.model.auth.gateways.TokenService;
import com.crediya.model.exceptions.user.InvalidAuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;

import static com.crediya.tokenservice.constants.Mesage.TOKEN_INVALID;

@Component
public class JwtProviderAdapter implements TokenService {
	
	private final Key key;
	
	public JwtProviderAdapter(
		@Value("${jwt.secret}") String secret
	) {
		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}
	
	@Override
	public Mono<UserClaims> validateToken(String token) {
		try {
			Claims claims = getClaimsFromToken(token);
			return Mono.just(new UserClaims(
				claims.get(AuthClaims.USER_ID.getValue(), Long.class),
				claims.getSubject(),
				claims.get(AuthClaims.IDENTIFICATION.getValue(), String.class),
				UserRole.valueOf(claims.get(AuthClaims.ROLE.getValue(), String.class))
			));
		} catch (Exception ex) {
			return Mono.error(new InvalidAuthException(TOKEN_INVALID));
		}
	}
	
	private Claims getClaimsFromToken(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}
}
