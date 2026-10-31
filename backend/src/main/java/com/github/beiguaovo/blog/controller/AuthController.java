package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.common.SecurityUtil;
import com.github.beiguaovo.blog.dto.LoginDTO;
import com.github.beiguaovo.blog.entity.User;
import com.github.beiguaovo.blog.service.AuthService;
import com.github.beiguaovo.blog.vo.LoginVO;
import com.github.beiguaovo.blog.dto.PasswordDTO;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @GetMapping("/me")
    public Result<User> me() {
        return Result.ok(authService.currentUser(SecurityUtil.uid()));
    }

    /** 无状态 JWT，登出由前端删 token 即可 */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO dto) {
        authService.changePassword(SecurityUtil.uid(), dto);
        return Result.ok();
    }
}