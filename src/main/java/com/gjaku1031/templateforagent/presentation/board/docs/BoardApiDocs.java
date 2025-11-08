package com.gjaku1031.templateforagent.presentation.board.docs;

import com.gjaku1031.templateforagent.presentation.board.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface BoardApiDocs {
    @Operation(summary = "Create board")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Long> createBoard(@RequestBody BoardCreateRequest request,
                                     @Parameter(hidden = true) Long userId);

    @Operation(summary = "Get board by id")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<BoardResponse> getBoard(@PathVariable("id") Long id);

    @Operation(summary = "Update board")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> updateBoard(@PathVariable("id") Long id,
                                     @RequestBody BoardUpdateRequest request);

    @Operation(summary = "Delete board")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> deleteBoard(@PathVariable("id") Long id);

    @Operation(summary = "Search boards (paged)")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Page<BoardResponse>> searchBoards(@RequestParam(name = "keyword", required = false) String keyword,
                                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                                     @RequestParam(name = "size", defaultValue = "20") int size);
}
