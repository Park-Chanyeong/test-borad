package com.example.demo.repository;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    //SELECT * FROM comment WHERE post_id = ? ORDER BY created_at ASC
    List<Comment> findAllByPostOrderByCreatedAtAsc(Post post);
    void deleteAllByPost(Post post);
}
