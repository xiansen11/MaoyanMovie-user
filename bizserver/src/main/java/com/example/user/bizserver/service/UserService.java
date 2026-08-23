package com.example.user.bizserver.service;

import com.example.user.api.dto.CinemaAccountReq;
import com.example.user.api.dto.UserLoginReq;
import com.example.user.api.dto.UserLoginResp;
import com.example.user.api.dto.UserRegisterReq;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * 校验用户名唯一 → BCrypt 加密 → 创建用户 role=0
     * → 初始化积分账户（赠送 100）→ 写积分流水 type=3
     *
     * @return 新用户 ID
     */
    Long register(UserRegisterReq req);

    /**
     * 用户登录
     * 校验用户名+密码 → 账号未禁用 → 生成 JWT
     * payload 含 userId/username/role/cinemaId
     */
    UserLoginResp login(UserLoginReq req);

    /**
     * 根据用户名查询用户
     */
    com.example.user.infrastructure.entity.UserDO queryByUsername(String username);

    /**
     * 查询积分余额
     */
    Integer queryPoints(Long userId);

    /**
     * 扣减积分（条件更新 points >= ?），返回是否成功
     */
    boolean deductPoints(Long userId, int points);

    /**
     * 返还积分（退票时）
     */
    boolean refundPoints(Long userId, int points);

    /**
     * 创建影院工作人员账号（影院审核通过后由 movie-server 调用）
     * role=2，绑定 cinemaId，不赠送积分
     *
     * @return 新账号 ID
     */
    Long createCinemaAccount(CinemaAccountReq req);
}
