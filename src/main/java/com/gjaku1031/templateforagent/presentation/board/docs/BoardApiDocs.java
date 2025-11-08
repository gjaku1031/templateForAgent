package com.gjaku1031.templateforagent.presentation.board.docs;

import com.gjaku1031.templateforagent.presentation.common.ErrorResponse;
import com.gjaku1031.templateforagent.presentation.board.dto.BoardCreateRequest;
import com.gjaku1031.templateforagent.presentation.board.dto.BoardResponse;
import com.gjaku1031.templateforagent.presentation.board.dto.BoardUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Board", description = "Board CRUD API")
public interface BoardApiDocs {

    @Operation(summary = "Create board (author=logged-in user)")
    @SecurityRequirement(name = "bearerAuth")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = BoardCreateRequest.class),
            examples = @ExampleObject(name = "create-board", value = "{\\n  \\\"title\\\": \\\"Hello\\\",\\n  \\\"content\\\": \\\"World\\\"\\n}")))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json",
                schema = @Schema(type = "integer", format = "int64"),
                examples = @ExampleObject(value = "1")))
    })
    ResponseEntity<Long> createBoard(@org.springframework.web.bind.annotation.RequestBody BoardCreateRequest request,
                                     Long userId);

    @Operation(summary = "Get board by id")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BoardResponse.class))),
        @ApiResponse(responseCode = "404", description = "Board not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<BoardResponse> getBoard(@PathVariable("id") Long id);

    @Operation(summary = "Update board")
    @SecurityRequirement(name = "bearerAuth")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = BoardUpdateRequest.class)))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    ResponseEntity<Void> updateBoard(@PathVariable("id") Long id,
                                     @org.springframework.web.bind.annotation.RequestBody BoardUpdateRequest request);

    @Operation(summary = "Delete board")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    ResponseEntity<Void> deleteBoard(@PathVariable("id") Long id);

    @Operation(summary = "Search boards (paged)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "boards-page", value = "{\\n  \\\"content\\\": [], \\\"pageable\\\": { \\\"pageNumber\\\": 0, \\\"pageSize\\\": 20 }\\n}")))
    })
    ResponseEntity<Page<BoardResponse>> searchBoards(@RequestParam(name = "keyword", required = false) String keyword,
                                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                                     @RequestParam(name = "size", defaultValue = "20") int size);
}

