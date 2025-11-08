package com.gjaku1031.templateforagent.presentation.user.dto;

import com.gjaku1031.templateforagent.domain.user.User;
import java.time.LocalDateTime;
import lombok.Value;

@Value
public class UserResponse {
    Long id;
    String username;
    String email;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

