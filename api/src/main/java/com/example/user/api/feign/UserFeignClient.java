package com.example.user.api.feign;

import com.example.common.response.Result;
import com.example.user.api.dto.CinemaAccountReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务 Feign 客户端（供 movie-server / order-server 等调用）
 * 路径前缀 /feign/user，网关不暴露
 */
@FeignClient(name = "user-server", contextId = "userFeignClient", path = "/feign/user")
public interface UserFeignClient {

    /**
     * 查询积分余额
     */
    @GetMapping("/points")
    Result<Integer> queryPoints(@RequestParam("userId") Long userId);

    /**
     * 扣减积分（条件更新 points >= ?，返回是否成功）
     */
    @PostMapping("/points/deduct")
    Result<Boolean> deductPoints(@RequestParam("userId") Long userId, @RequestParam("points") int points);

    /**
     * 返还积分（退票时）
     */
    @PostMapping("/points/refund")
    Result<Boolean> refundPoints(@RequestParam("userId") Long userId, @RequestParam("points") int points);

    /**
     * 创建影院工作人员账号（影院审核通过后调用，role=2，绑定 cinemaId）
     * 不赠送积分，仅建账号
     */
    @PostMapping("/account/cinema")
    Result<Long> createCinemaAccount(@RequestBody CinemaAccountReq req);
}
