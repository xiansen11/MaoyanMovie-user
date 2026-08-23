package com.example.user.apiserver.controller.feign;

import com.example.common.response.Result;
import com.example.user.api.dto.CinemaAccountReq;
import com.example.user.bizserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务 Feign 接口实现 /feign/user（不经网关，服务间调用）
 */
@RestController
@RequestMapping("/feign/user")
public class UserFeignController {

    @Autowired
    private UserService userService;

    @GetMapping("/points")
    public Result<Integer> queryPoints(@RequestParam("userId") Long userId) {
        return Result.success(userService.queryPoints(userId));
    }

    @PostMapping("/points/deduct")
    public Result<Boolean> deductPoints(@RequestParam("userId") Long userId, @RequestParam("points") int points) {
        return Result.success(userService.deductPoints(userId, points));
    }

    @PostMapping("/points/refund")
    public Result<Boolean> refundPoints(@RequestParam("userId") Long userId, @RequestParam("points") int points) {
        return Result.success(userService.refundPoints(userId, points));
    }

    @PostMapping("/account/cinema")
    public Result<Long> createCinemaAccount(@RequestBody CinemaAccountReq req) {
        return Result.success(userService.createCinemaAccount(req));
    }
}
