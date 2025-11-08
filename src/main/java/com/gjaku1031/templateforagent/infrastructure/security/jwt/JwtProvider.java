package com.gjaku1031.templateforagent.infrastructure.security.jwt;

import com.gjaku1031.templateforagent.infrastructure.config.JwtConfig;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, String> redisTemplate;
    @Getter
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        String secret = jwtConfig.getSecret();
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    public String createAccessToken(String username, String role) {
        Map<String, String> claims = Map.of("role", role);
        return createToken(username, claims, jwtConfig.getAccessTokenExpiration());
    }

    public String createRefreshToken(String username) {
        Map<String, String> claims = Map.of();
        String refreshToken = createToken(username, claims, jwtConfig.getRefreshTokenExpiration());
        redisTemplate.opsForValue().set(
                "refresh:" + username,
                refreshToken,
                jwtConfig.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
        return refreshToken;
    }

    private String createToken(String subject, Map<String, String> claims, long tokenExpiration) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenExpiration);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .id(Long.toHexString(System.nanoTime()))
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
}

