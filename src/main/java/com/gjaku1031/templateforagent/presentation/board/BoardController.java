package com.gjaku1031.templateforagent.presentation.board;

import com.gjaku1031.templateforagent.application.board.BoardService;
import com.gjaku1031.templateforagent.presentation.board.docs.BoardApiDocs;
import com.gjaku1031.templateforagent.presentation.board.dto.BoardCreateRequest;
import com.gjaku1031.templateforagent.presentation.board.dto.BoardResponse;
import com.gjaku1031.templateforagent.presentation.board.dto.BoardUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController implements BoardApiDocs {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<Long> createBoard(@RequestBody @Valid BoardCreateRequest request,
                                            @com.gjaku1031.templateforagent.presentation.common.annotation.CurrentUser Long userId) {
        Long id = boardService.create(userId, request.getTitle(), request.getContent());
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<BoardResponse> getBoard(@PathVariable("id") Long id) {
        BoardResponse dto = boardService.get(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<Void> updateBoard(@PathVariable("id") Long id,
                                            @RequestBody @Valid BoardUpdateRequest request) {
        boardService.update(id, request.getTitle(), request.getContent());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteBoard(@PathVariable("id") Long id) {
        boardService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Override
    public ResponseEntity<Page<BoardResponse>> searchBoards(@RequestParam(name = "keyword", required = false) String keyword,
                                                            @RequestParam(name = "page", defaultValue = "0") int page,
                                                            @RequestParam(name = "size", defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        var boards = boardService.search(keyword, pageable);
        var responses = boards.getContent().stream().map(BoardResponse::from).collect(Collectors.toList());
        var result = new PageImpl<>(responses, pageable, boards.getTotalElements());
        return ResponseEntity.ok(result);
    }
}

