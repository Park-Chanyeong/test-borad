package com.example.demo.controller;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.PostDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostLikeService;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/web/posts")
@RequiredArgsConstructor
public class PostViewController {

    private final PostService postService;
    private final CommentService commentService;
    private final PostLikeService postLikeService;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        model.addAttribute("posts", postService.findAllPaged(page, size));
        return "posts/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("post", postService.findById(id));
        model.addAttribute("comments", commentService.findByPostId(id));
        model.addAttribute("commentDto", new CommentDto());

        String username = authentication != null && authentication.isAuthenticated()
                ? authentication.getName() : null;
        model.addAttribute("likeCount", postLikeService.countByPostId(id));
        model.addAttribute("hasLiked", postLikeService.hasLiked(id, username));
        return "posts/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("dto", new PostDto());
        return "posts/form";
    }

    @PostMapping
    public String create(@ModelAttribute PostDto dto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/web/login";
        }
        postService.create(dto, authentication.getName());
        return "redirect:/web/posts";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication authentication) {
        Post post = postService.findByIdInternal(id);
        checkOwnership(post, authentication);
        model.addAttribute("post", post);
        return "posts/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute PostDto dto, Authentication authentication) {
        Post post = postService.findByIdInternal(id);
        checkOwnership(post, authentication);
        postService.update(id, dto);
        return "redirect:/web/posts/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        Post post = postService.findByIdInternal(id);
        checkOwnership(post, authentication);
        postService.delete(id);
        return "redirect:/web/posts";
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id, @ModelAttribute CommentDto dto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/web/login";
        }
        commentService.create(id, dto, authentication.getName());
        return "redirect:/web/posts/" + id + "#comments";
    }

    @PostMapping("/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long id, @PathVariable Long commentId, Authentication authentication) {
        Comment comment = commentService.findById(commentId);
        if (authentication == null || !authentication.isAuthenticated()
                || comment.getAuthorUsername() == null
                || !comment.getAuthorUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
        commentService.delete(commentId);
        return "redirect:/web/posts/" + id + "#comments";
    }

    @PostMapping("/{id}/like")
    public String toggleLike(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/web/login";
        }
        postLikeService.toggle(id, authentication.getName());
        return "redirect:/web/posts/" + id;
    }

    private void checkOwnership(Post post, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || post.getAuthor() == null
                || !post.getAuthor().getUsername().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
    }
}
