package com.gjaku1031.templateforagent.application.auth;

import com.gjaku1031.templateforagent.infrastructure.config.JwtConfig;
import com.gjaku1031.templateforagent.infrastructure.security.jwt.JwtProvider;
import com.gjaku1031.templateforagent.infrastructure.security.jwt.JwtUtil;
import com.gjaku1031.templateforagent.presentation.user.dto.TokenResponse;
import com.gjaku1031.templateforagent.common.error.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;

    public TokenResponse login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String accessToken = jwtProvider.createAccessToken(username, role);
        String refreshToken = jwtProvider.createRefreshToken(username);
        return new TokenResponse("Bearer", accessToken, jwtConfig.getAccessTokenExpiration(), refreshToken);
    }

    public TokenResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken) || jwtUtil.isExpired(refreshToken) || !jwtUtil.isValidRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);
        if (role == null) {
            Authentication auth = jwtUtil.getAuthentication(refreshToken);
            role = auth.getAuthorities().iterator().next().getAuthority();
        }
        String newAccess = jwtProvider.createAccessToken(username, role);
        String newRefresh = jwtProvider.createRefreshToken(username);
        return new TokenResponse("Bearer", newAccess, jwtConfig.getAccessTokenExpiration(), newRefresh);
    }

    public void logout(String accessToken) {
        if (accessToken != null) {
            jwtUtil.addBlacklist(accessToken);
            jwtUtil.deleteRefreshToken(accessToken);
        }
    }
}

