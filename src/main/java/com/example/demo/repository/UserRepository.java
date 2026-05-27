package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Optional<User> findByUsername(String username);
    //
    //  결과가 있을 수도, 없을 수도 있을 때 쓰는 래퍼
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}