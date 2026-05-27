package com.example.demo;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashGen {
    @Test
    void gen() {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        System.out.println("admin1234 = " + enc.encode("admin1234"));
        System.out.println("user1234  = " + enc.encode("user1234"));
    }
}
