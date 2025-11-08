package com.gjaku1031.templateforagent.presentation.user.docs;

import com.gjaku1031.templateforagent.presentation.common.ErrorResponse;
import com.gjaku1031.templateforagent.presentation.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;

public interface UserAuthApiDocs {

    @Operation(summary = "Register user", description = "회원 가입 후 사용자 ID 반환",
            requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "register-example", value = "{\\n  \\\"username\\\": \\\"user1\\\",\\n  \\\"email\\\": \\\"user1@example.com\\\",\\n  \\\"password\\\": \\\"pass1234\\\"\\n}"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Long> register(UserRegisterRequest request);

    @Operation(summary = "Login", description = "로그인 후 액세스/리프레시 토큰 발급",
            requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "login-example", value = "{\\n  \\\"username\\\": \\\"user1\\\",\\n  \\\"password\\\": \\\"pass1234\\\"\\n}"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    ResponseEntity<TokenResponse> login(LoginRequest request);

    @Operation(summary = "Refresh token", description = "리프레시 토큰으로 새 액세스 토큰 발급",
            requestBody = @RequestBody(required = false, content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "refresh-example", value = "{\\n  \\\"refreshToken\\\": \\\"<refresh-token>\\\"\\n}"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    ResponseEntity<TokenResponse> refresh(String authorization, RefreshRequest body);

    @Operation(summary = "Logout", description = "액세스 토큰 블랙리스트 등록 후 로그아웃")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> logout(String authorization);

    @Operation(summary = "Me", description = "현재 로그인한 사용자 정보")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<UserResponse> me(Long userId);
}

