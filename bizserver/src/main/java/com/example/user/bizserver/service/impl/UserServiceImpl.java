package com.example.user.bizserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.exception.BizException;
import com.example.common.jwt.JwtUtil;
import com.example.common.response.ResultCode;
import com.example.user.api.dto.CinemaAccountReq;
import com.example.user.api.dto.UserLoginReq;
import com.example.user.api.dto.UserLoginResp;
import com.example.user.api.dto.UserRegisterReq;
import com.example.user.bizserver.service.UserService;
import com.example.user.infrastructure.entity.PointsLogDO;
import com.example.user.infrastructure.entity.UserAccountDO;
import com.example.user.infrastructure.entity.UserDO;
import com.example.user.infrastructure.mapper.PointsLogMapper;
import com.example.user.infrastructure.mapper.UserAccountMapper;
import com.example.user.infrastructure.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    /** 注册赠送积分 */
    private static final int REGISTER_GIFT_POINTS = 100;
    /** 积分流水类型：3=注册赠送 */
    private static final int POINTS_TYPE_REGISTER_GIFT = 3;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private PointsLogMapper pointsLogMapper;
    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserLoginResp login(UserLoginReq req) {
        // 1. 查用户
        UserDO user = queryByUsername(req.getUsername());
        if (user == null) {
            throw new BizException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        // 2. 校验密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        // 3. 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }
        // 4. 生成 JWT
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole(), user.getCinemaId());

        UserLoginResp resp = new UserLoginResp();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setRole(user.getRole());
        resp.setCinemaId(user.getCinemaId());

        log.info("[USER-LOGIN] 用户登录成功 userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterReq req) {
        // 1. 校验用户名唯一
        UserDO exist = queryByUsername(req.getUsername());
        if (exist != null) {
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }

        // 2. BCrypt 加密密码，创建用户（role=0 普通用户）
        UserDO user = new UserDO();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        user.setPhone(req.getPhone());
        user.setRole(0);
        user.setStatus(0);
        userMapper.insert(user);

        // 3. 初始化积分账户（赠送 100）
        UserAccountDO account = new UserAccountDO();
        account.setUserId(user.getId());
        account.setPoints(REGISTER_GIFT_POINTS);
        account.setVersion(0);
        userAccountMapper.insert(account);

        // 4. 写积分流水 type=3 注册赠送
        PointsLogDO pointsLog = new PointsLogDO();
        pointsLog.setUserId(user.getId());
        pointsLog.setChange(REGISTER_GIFT_POINTS);
        pointsLog.setBalance(REGISTER_GIFT_POINTS);
        pointsLog.setType(POINTS_TYPE_REGISTER_GIFT);
        pointsLog.setRemark("注册赠送");
        pointsLogMapper.insert(pointsLog);

        log.info("[USER-REGISTER] 用户注册成功 userId={}, username={}", user.getId(), user.getUsername());
        return user.getId();
    }

    @Override
    public UserDO queryByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, username));
    }

    @Override
    public Integer queryPoints(Long userId) {
        UserAccountDO account = userAccountMapper.selectOne(
                new LambdaQueryWrapper<UserAccountDO>().eq(UserAccountDO::getUserId, userId));
        return account == null ? 0 : account.getPoints();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Long userId, int points) {
        int rows = userAccountMapper.deductPoints(userId, points);
        if (rows > 0) {
            // 写积分流水 type=2 下单消费
            PointsLogDO pointsLog = new PointsLogDO();
            pointsLog.setUserId(userId);
            pointsLog.setChange(-points);
            pointsLog.setBalance(queryPoints(userId));
            pointsLog.setType(2);
            pointsLog.setRemark("下单消费");
            pointsLogMapper.insert(pointsLog);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundPoints(Long userId, int points) {
        int rows = userAccountMapper.refundPoints(userId, points);
        if (rows > 0) {
            // 写积分流水 type=4 退票返还
            PointsLogDO pointsLog = new PointsLogDO();
            pointsLog.setUserId(userId);
            pointsLog.setChange(points);
            pointsLog.setBalance(queryPoints(userId));
            pointsLog.setType(4);
            pointsLog.setRemark("退票返还");
            pointsLogMapper.insert(pointsLog);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCinemaAccount(CinemaAccountReq req) {
        // 1. 校验用户名唯一
        UserDO exist = queryByUsername(req.getUsername());
        if (exist != null) {
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 创建影院工作人员账号 role=2，绑定 cinemaId，不赠送积分
        UserDO user = new UserDO();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        user.setRole(2);
        user.setCinemaId(req.getCinemaId());
        user.setStatus(0);
        userMapper.insert(user);

        log.info("[USER-CINEMA-ACCOUNT] 影院账号创建 userId={}, cinemaId={}", user.getId(), req.getCinemaId());
        return user.getId();
    }
}
