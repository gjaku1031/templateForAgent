package com.gjaku1031.templateforagent.domain.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardRepositoryCustom {
    Page<Board> search(String keyword, Pageable pageable);
}

