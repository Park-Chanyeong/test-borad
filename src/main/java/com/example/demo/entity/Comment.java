package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment extends BaseTimeEntity {

    @Id // 이 필드가 PK(기본키)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK를 DB가 자동으로 1씩 증가시켜 생성해줌
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY) // 관계매핑 어노테이션? -> N:1관계 (여러 게시글이 한 유저속함)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore // Jackson이 JSON으로 변환할 때 이 필드를 무시
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;

    public String getAuthorUsername() {
        return author != null ? author.getUsername() : null;
    }
}
