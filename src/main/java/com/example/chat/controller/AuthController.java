package com.example.chat.controller;

import com.example.chat.dto.LoginRequest;
import com.example.chat.dto.LoginResponse;
import com.example.chat.dto.RegisterRequest;
import com.example.chat.dto.UpdatePasswordRequest;
import com.example.chat.dto.UpdateProfileRequest;
import com.example.chat.dto.UserVO;
import com.example.chat.security.UserContext;
import com.example.chat.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserVO me() {
        return authService.me(UserContext.require());
    }

    @PostMapping("/password")
    public void updatePassword(@Valid @RequestBody UpdatePasswordRequest req) {
        authService.updatePassword(UserContext.require(), req);
    }

    /** 更新昵称 / 头像，字段为 null 表示不修改 */
    @PostMapping("/profile")
    public UserVO updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        return authService.updateProfile(UserContext.require(), req);
    }
}