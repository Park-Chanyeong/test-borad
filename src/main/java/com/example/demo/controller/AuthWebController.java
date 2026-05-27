package com.example.demo.controller;

import com.example.demo.dto.RegisterDto;
import com.example.demo.exception.DuplicateException;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthService authService;

    @GetMapping("/")
    public String home() {
        return "redirect:/web/posts";
    }

    @GetMapping("/web/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return "auth/login";
    }

    @GetMapping("/web/register")
    public String registerForm(Model model) {
        model.addAttribute("dto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/web/register")
    public String register(@ModelAttribute RegisterDto dto, Model model) {
        try {
            authService.register(dto);
            return "redirect:/web/login?registered";
        } catch (DuplicateException e) {
            model.addAttribute("error", "이미 존재하는 사용자입니다.");
            model.addAttribute("dto", dto);
            return "auth/register";
        }
    }
}
