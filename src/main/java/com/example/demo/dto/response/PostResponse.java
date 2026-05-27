package com.example.demo.dto.response;

import com.example.demo.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Post 엔티티를 API 응답으로 변환하는 DTO.
 * 엔티티를 직접 반환하면 author(User) 객체가 포함되어 민감 정보가 노출될 수 있으므로
 * 응답에 필요한 필드만 추려서 내보낸다.
 */
@Getter
public class PostResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String authorUsername;
    private final LocalDateTime createdAt;
    private final int viewCount;

    private PostResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorUsername = post.getAuthorUsername();
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViewCount();
    }

    /** Post 엔티티 → PostResponse 변환 진입점 */
    public static PostResponse from(Post post) {
        return new PostResponse(post);
    }
}