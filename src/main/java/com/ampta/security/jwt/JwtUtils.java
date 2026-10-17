package com.ampta.security.jwt;

import com.ampta.entity.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    private final SecretKey secretKey;
    private final Long jwtExpirationMs;
    private final Long jwtRefreshExpirationMs;

    public JwtUtils(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expirationMs}") Long jwtExpirationMs,
            @Value("${app.jwt.refreshExpirationMs:604800000}") Long jwtRefreshExpirationMs
    ){
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtRefreshExpirationMs = jwtRefreshExpirationMs;
    }

    public Long getJwtRefreshExpirationMs() {
        return jwtRefreshExpirationMs;
    }

    public String generateToken(String email, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)                 // Header.Payload.Signature
                .claim("role", role)            // Custom claim: USER or ADMIN
                .issuedAt(now)                  // Token creation time
                .expiration(expiryDate)         // Expiration time (15 mins)
                .signWith(secretKey, Jwts.SIG.HS256) // HMAC SHA-256 signature key
                .compact();
    }

    public String generatePreAuthToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 300000); // 5 minutes validity

        return Jwts.builder()
                .subject(email)
                .claim("type", "PRE_AUTH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getTokenTypeFromToken(String token) {
        try {
            Object type = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("type");
            return type != null ? type.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }


}
