package com.example.user.apiserver.controller.api;

import com.example.common.response.Result;
import com.example.user.api.dto.UserLoginReq;
import com.example.user.api.dto.UserLoginResp;
import com.example.user.api.dto.UserRegisterReq;
import com.example.user.bizserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * C 端用户接口 /api/user
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    /**
     * 注册 POST /api/user/register
     */
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody UserRegisterReq req) {
        return Result.success(userService.register(req));
    }

    /**
     * 登录 POST /api/user/login（返回 JWT）
     */
    @PostMapping("/login")
    public Result<UserLoginResp> login(@Valid @RequestBody UserLoginReq req) {
        return Result.success(userService.login(req));
    }
}
