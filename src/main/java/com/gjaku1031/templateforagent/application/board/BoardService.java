package com.gjaku1031.templateforagent.application.board;

import com.gjaku1031.templateforagent.common.error.exception.NotFoundException;
import com.gjaku1031.templateforagent.domain.board.Board;
import com.gjaku1031.templateforagent.domain.board.BoardRepository;
import com.gjaku1031.templateforagent.domain.user.User;
import com.gjaku1031.templateforagent.domain.user.UserRepository;
import com.gjaku1031.templateforagent.presentation.board.dto.BoardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long authorId, String title, String content) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new NotFoundException("Author not found"));
        Board saved = boardRepository.save(Board.builder()
            .author(author)
            .title(title)
            .content(content)
            .build());
        return saved.getId();
    }

    public BoardResponse get(Long id) {
        Board board = getEntity(id);
        return BoardResponse.from(board);
    }

    @Transactional
    public void update(Long id, String title, String content) {
        Board board = getEntity(id);
        board.change(title, content);
    }

    @Transactional
    public void delete(Long id) {
        Board board = getEntity(id);
        boardRepository.delete(board);
    }

    public Page<Board> search(String keyword, Pageable pageable) {
        return boardRepository.search(keyword, pageable);
    }

    private Board getEntity(Long id) {
        return boardRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Board not found"));
    }
}

