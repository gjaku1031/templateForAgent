package com.gjaku1031.templateforagent.domain.board;

import static com.gjaku1031.templateforagent.domain.board.QBoard.board;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

/**
 * BoardRepository 커스텀 구현(QueryDSL)
 */
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Board> search(String keyword, Pageable pageable) {
        List<Board> content = queryFactory
            .selectFrom(board)
            .leftJoin(board.author).fetchJoin()
            .where(containsKeyword(keyword))
            .orderBy(board.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        var countQuery = queryFactory
            .select(board.count())
            .from(board)
            .where(containsKeyword(keyword));

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchOne());
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return board.title.containsIgnoreCase(keyword)
            .or(board.content.containsIgnoreCase(keyword));
    }
}

