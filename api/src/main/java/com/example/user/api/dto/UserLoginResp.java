package com.example.user.api.dto;

import lombok.Data;

/**
 * 登录响应（含 token 与基本信息）
 */
@Data
public class UserLoginResp {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private Integer role;
    private Long cinemaId;
}
