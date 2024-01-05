package com.walnutek.fermentationtank.config.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


@Service
public class JwtService {

	@Value("${app.jwt-key}")
    private String KEY;

    @Value("${app.jwt-expire-min}")
    private Integer EXPIRE_MIN;

    public String generateToken(AuthUser authUser) {
    	final var claims = Jwts.claims()
	    	.add("userId", authUser.getUserId())
	    	.add("account", authUser.getAccount())
	    	.add("name", authUser.getName())
	    	.add("email", authUser.getEmail())
	    	.add("role", authUser.getRole())
	    	.expiration(Date.from(Instant.now().plus(EXPIRE_MIN, ChronoUnit.MINUTES)))
	    	.issuer("walnutek")
	    	.build();
    	
        return Jwts.builder()
                .claims(claims)
                .signWith(Keys.hmacShaKeyFor(KEY.getBytes()))
                .compact();
    }

    public String refreshToken(String token) {
        final var claims = (Claims) Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(KEY.getBytes()))
                .build()
                .parse(token)
                .getPayload();
        
        return Jwts.builder()
        		.claims(claims)
        		.expiration(Date.from(Instant.now().plus(EXPIRE_MIN, ChronoUnit.MINUTES)))
                .signWith(Keys.hmacShaKeyFor(KEY.getBytes()))
                .compact();
    }

    public AuthUser reconstructAuthUserFromToken(String token) {
        var claims = (Claims) Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(KEY.getBytes()))
                .build()
                .parse(token)
                .getPayload();
        
        return AuthUser.of(claims);
    }

    public boolean isTokenExpire(String token) {
        boolean isExpire = false;
        try {
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(KEY.getBytes()))
                .build()
                .parse(token);
        } catch (final ExpiredJwtException e) {
            isExpire = true;
        }
        return isExpire;
    }
    
}
