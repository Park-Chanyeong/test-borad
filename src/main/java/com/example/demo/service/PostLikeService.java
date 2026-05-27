package com.example.demo.service;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.User;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostService postService;
    private final UserRepository userRepository;

    //좋아요 누르면 추가, 이미 눌렀으면 취소 (토글)
    @Transactional
    public boolean toggle(Long postId, String username) {
        Post post = postService.findByIdInternal(postId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        if (postLikeRepository.existsByPostAndUser(post, user)) {
            postLikeRepository.deleteByPostAndUser(post, user);
            return false;
        } else {
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);
            postLikeRepository.save(like);
            return true;
        }
    }
    // 게시글 좋아요 수 조회
    public long countByPostId(Long postId) {
        Post post = postService.findByIdInternal(postId);
        return postLikeRepository.countByPost(post);
    }
    // 현재 유저가 이미 눌렀는지 확인
    public boolean hasLiked(Long postId, String username) {
        if (username == null) return false;
        Post post = postService.findByIdInternal(postId);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        return postLikeRepository.existsByPostAndUser(post, user);
    }
}
