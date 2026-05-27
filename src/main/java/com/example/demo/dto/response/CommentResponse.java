package com.example.demo.dto.response;

import com.example.demo.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Comment 엔티티를 API 응답으로 변환하는 DTO.
 * 엔티티 직접 반환 시 author(User) 객체가 포함되어 민감 정보가 노출될 수 있으므로
 * 필요한 필드만 추려서 내보낸다.
 */
@Getter
public class CommentResponse {

    private final Long id;
    private final String content;
    private final String authorUsername;
    private final LocalDateTime createdAt;

    private CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorUsername = comment.getAuthorUsername();
        this.createdAt = comment.getCreatedAt();
    }

    /** Comment 엔티티 → CommentResponse 변환 진입점 */
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment);
    }
}