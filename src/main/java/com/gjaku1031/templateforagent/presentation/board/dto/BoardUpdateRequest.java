package com.gjaku1031.templateforagent.presentation.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class BoardUpdateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
}

