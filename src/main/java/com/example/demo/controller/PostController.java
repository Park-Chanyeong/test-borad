package com.example.demo.controller;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Post;
import com.example.demo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Posts", description = "게시글 CRUD API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "전체 게시글 목록을 반환합니다. 인증 불필요.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public List<Post> getAll() {
        return postService.findAll();
    }

    @Operation(summary = "게시글 단건 조회", description = "ID로 특정 게시글을 조회합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "존재하지 않는 게시글")
    })
    @GetMapping("/{id}")
    public Post getOne(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id) {
        return postService.findById(id);
    }

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다. JWT 인증 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "403", description = "인증 실패")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public Post create(
            @RequestBody PostDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return postService.create(dto, userDetails.getUsername());
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다. JWT 인증 필요, 작성자 본인만 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 인증 실패")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public Post update(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id,
            @RequestBody PostDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.findById(id);
        checkOwnership(post, userDetails.getUsername());
        return postService.update(id, dto);
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. JWT 인증 필요, 작성자 본인만 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 인증 실패")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.findById(id);
        checkOwnership(post, userDetails.getUsername());
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void checkOwnership(Post post, String username) {
        if (post.getAuthor() == null || !post.getAuthor().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
    }
}