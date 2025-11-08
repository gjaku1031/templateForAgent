package com.gjaku1031.templateforagent.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 설정 프로퍼티 바인딩
 * <p>secret, 만료시간(ms), issuer를 매핑.</p>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private long accessTokenExpiration; // milliseconds
    private long refreshTokenExpiration; // milliseconds
    private String issuer;

    // reserved for cookie-based flow later: removed for now
}
