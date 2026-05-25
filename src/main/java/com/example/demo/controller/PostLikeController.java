package com.example.demo.controller;

import com.example.demo.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Likes", description = "좋아요 API")
@RestController
@RequestMapping("/api/posts/{postId}/like")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(summary = "좋아요 수 조회", description = "게시글의 좋아요 수를 반환합니다. 인증 불필요.")
    @GetMapping
    public Map<String, Object> getLikeInfo(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = postLikeService.countByPostId(postId);
        String username = userDetails != null ? userDetails.getUsername() : null;
        boolean hasLiked = postLikeService.hasLiked(postId, username);
        return Map.of("count", count, "hasLiked", hasLiked);
    }

    @Operation(summary = "좋아요 토글", description = "좋아요를 누르거나 취소합니다. JWT 인증 필요.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public Map<String, Object> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean liked = postLikeService.toggle(postId, userDetails.getUsername());
        long count = postLikeService.countByPostId(postId);
        return Map.of("liked", liked, "count", count);
    }
}
