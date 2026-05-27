package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    //전체 목록을 한 번에 다 가져오면 데이터가 많을 때 느려지니까, 페이지 단위로 잘라서 가져옴
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}