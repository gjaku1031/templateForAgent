package com.gjaku1031.templateforagent.infrastructure.security.jwt;

import com.gjaku1031.templateforagent.infrastructure.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtConfig jwtConfig;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;

    private SecretKey getSecretKey() { return jwtProvider.getSecretKey(); }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            if (claims.getExpiration().before(new Date())) return false;
            return jwtConfig.getIssuer().equals(claims.getIssuer());
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsername(String token) { return getClaims(token).getSubject(); }
    public boolean hasRole(String token) { return getClaims(token).get("role") != null; }
    public String getRole(String token) { Object r = getClaims(token).get("role"); return r == null ? null : r.toString(); }
    private String getJti(String token) { return getClaims(token).getId(); }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) { return e.getClaims(); }
    }

    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public void addBlacklist(String accessToken) {
        redisTemplate.opsForValue().set(
                "blacklist:" + getJti(accessToken),
                "true",
                jwtConfig.getAccessTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + getJti(token)));
    }

    public void deleteRefreshToken(String accessToken) { redisTemplate.delete("refresh:" + getUsername(accessToken)); }

    public boolean isValidRefreshToken(String refreshToken) {
        String stored = redisTemplate.opsForValue().get("refresh:" + getUsername(refreshToken));
        return stored != null && stored.equals(refreshToken);
    }

    public boolean isExpired(String token) {
        try { return getClaims(token).getExpiration().before(new Date()); } catch (Exception e) { return true; }
    }
}
