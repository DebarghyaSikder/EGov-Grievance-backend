package com.grievance.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JwtUtils {

    private final String SECRET = "THIS_IS_A_TEMP_SECRET_CHANGE_IT_LATER_32CHARS";

    private Claims extractAll(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAll(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return extractAll(token).get("role", String.class);
    }
}