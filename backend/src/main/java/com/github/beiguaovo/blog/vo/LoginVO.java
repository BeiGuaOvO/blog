package com.github.beiguaovo.blog.vo;

import com.github.beiguaovo.blog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private String token;
    private String tokenType;
    private long expiresIn;
    private User user;

    public LoginVO(String token, long expiresIn, User user) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
    }
}