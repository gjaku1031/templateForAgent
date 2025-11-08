package com.gjaku1031.templateforagent.presentation.user;

import com.gjaku1031.templateforagent.application.auth.AuthService;
import com.gjaku1031.templateforagent.application.user.UserService;
import com.gjaku1031.templateforagent.domain.user.User;
import com.gjaku1031.templateforagent.presentation.common.annotation.*;
import com.gjaku1031.templateforagent.presentation.user.docs.UserApiDocs;
import com.gjaku1031.templateforagent.presentation.user.docs.UserAuthApiDocs;
import com.gjaku1031.templateforagent.presentation.user.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApiDocs, UserAuthApiDocs {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Long> register(@RequestBody @Valid UserRegisterRequest request) {
        Long id = userService.register(request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(id);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                 @RequestBody(required = false) RefreshRequest body) {
        String refreshToken = extractToken(authorization);
        if (refreshToken == null && body != null) {
            refreshToken = body.getRefreshToken();
        }
        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String accessToken = extractToken(authorization);
        authService.logout(accessToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @IsAdmin
    @Override
    public ResponseEntity<Long> createUser(@RequestBody @Valid UserCreateRequest request) {
        Long id = userService.create(request.getUsername(), request.getEmail());
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    @AdminOrUserSelfRead
    @Override
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") Long id) {
        User user = userService.get(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/{id}")
    @AdminOrUserSelfUpdate
    @Override
    public ResponseEntity<Void> updateUser(@PathVariable("id") Long id, @RequestBody @Valid UserUpdateRequest request) {
        userService.update(id, request.getUsername(), request.getEmail());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @AdminOrUserSelfDelete
    @Override
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @IsAdmin
    @Override
    public ResponseEntity<Page<UserResponse>> listUsers(@RequestParam(name = "page", defaultValue = "0") int page,
                                                        @RequestParam(name = "size", defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        var users = userService.list(pageable);
        var responses = users.getContent().stream().map(UserResponse::from).collect(Collectors.toList());
        var result = new PageImpl<>(responses, pageable, users.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> me(@com.gjaku1031.templateforagent.presentation.common.annotation.CurrentUser Long userId) {
        User user = userService.get(userId);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}

