package com.gjaku1031.templateforagent.domain.board;

import com.gjaku1031.templateforagent.domain.BaseTimeEntity;
import com.gjaku1031.templateforagent.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 게시글 엔티티
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    public void change(String title, String content) {
        this.title = title;
        this.content = content;
    }
}

