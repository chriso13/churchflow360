package com.churchflow360.smallgroups.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.*;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private Key key() { return Keys.hmacShaKeyFor(secret.getBytes()); }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token) { return extractAllClaims(token).getSubject(); }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    public boolean isValid(String token) {
        try { extractAllClaims(token); return true; }
        catch (Exception e) { return false; }
    }
}
