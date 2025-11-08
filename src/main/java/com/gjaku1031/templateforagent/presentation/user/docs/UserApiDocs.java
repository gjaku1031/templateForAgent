package com.gjaku1031.templateforagent.presentation.user.docs;

import com.gjaku1031.templateforagent.presentation.common.ErrorResponse;
import com.gjaku1031.templateforagent.presentation.user.dto.UserCreateRequest;
import com.gjaku1031.templateforagent.presentation.user.dto.UserResponse;
import com.gjaku1031.templateforagent.presentation.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "User CRUD API")
@SecurityRequirement(name = "bearerAuth")
public interface UserApiDocs {

    @Operation(summary = "Create user", description = "관리자 전용 사용자 생성")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = UserCreateRequest.class),
            examples = @ExampleObject(name = "create-user", value = "{\\n  \\\"username\\\": \\\"newuser\\\",\\n  \\\"email\\\": \\\"newuser@example.com\\\"\\n}")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json",
                    schema = @Schema(type = "integer", format = "int64"),
                    examples = @ExampleObject(value = "1"))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Long> createUser(@RequestBody UserCreateRequest request);

    @Operation(summary = "Get user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserResponse> getUser(@PathVariable("id") Long id);

    @Operation(summary = "Update user", description = "관리자 또는 본인만 가능")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = UserUpdateRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    ResponseEntity<Void> updateUser(@PathVariable("id") Long id, @RequestBody UserUpdateRequest request);

    @Operation(summary = "Delete user", description = "관리자 또는 본인만 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    ResponseEntity<Void> deleteUser(@PathVariable("id") Long id);

    @Operation(summary = "List users (paged)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "users-page", value = "{\\n  \\\"content\\\": [], \\\"pageable\\\": { \\\"pageNumber\\\": 0, \\\"pageSize\\\": 20 }\\n}")))
    })
    ResponseEntity<Page<UserResponse>> listUsers(@RequestParam(name = "page", defaultValue = "0") int page,
                                                 @RequestParam(name = "size", defaultValue = "20") int size);
}
