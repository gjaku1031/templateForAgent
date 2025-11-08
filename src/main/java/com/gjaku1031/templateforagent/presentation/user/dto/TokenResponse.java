package com.gjaku1031.templateforagent.presentation.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String tokenType;
    private String accessToken;
    private long expiresIn;
    private String refreshToken;
}

