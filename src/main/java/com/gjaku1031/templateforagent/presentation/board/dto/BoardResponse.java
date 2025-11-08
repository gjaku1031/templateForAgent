package com.gjaku1031.templateforagent.presentation.board.dto;

import com.gjaku1031.templateforagent.domain.board.Board;
import java.time.LocalDateTime;
import lombok.Value;

@Value
public class BoardResponse {
    Long id;
    String title;
    String content;
    Long authorId;
    String authorUsername;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getAuthor().getId(),
                board.getAuthor().getUsername(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}

