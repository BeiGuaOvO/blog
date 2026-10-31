package com.github.beiguaovo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.beiguaovo.blog.common.BizException;
import com.github.beiguaovo.blog.dto.LoginDTO;
import com.github.beiguaovo.blog.entity.User;
import com.github.beiguaovo.blog.mapper.UserMapper;
import com.github.beiguaovo.blog.security.JwtUtil;
import com.github.beiguaovo.blog.vo.LoginVO;
import com.github.beiguaovo.blog.dto.PasswordDTO;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(403, "账号已被禁用");
        }
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, jwtUtil.getExpireSeconds(), user);
    }

    public User currentUser(Long uid) {
        if (uid == null) {
            throw new BizException(401, "未登录");
        }
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        return user;
    }

    public void changePassword(Long uid, PasswordDTO dto) {
        if (uid == null) {
            throw new BizException(401, "未登录");
        }
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(400, "原密码错误");
        }
        if (!StringUtils.hasText(dto.getNewPassword())) {
            throw new BizException(400, "新密码不能为空");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BizException(400, "新密码不能与原密码相同");
        }

        // 只更新 password 字段
        User update = new User();
        update.setId(uid);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }
}