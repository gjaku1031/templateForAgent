package com.gjaku1031.templateforagent.presentation.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserCreateRequest {
    @NotBlank
    private String username;
    @Email
    @NotBlank
    private String email;
}

