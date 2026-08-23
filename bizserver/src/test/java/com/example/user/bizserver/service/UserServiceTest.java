package com.example.user.bizserver.service;

import com.example.common.exception.BizException;
import com.example.user.api.dto.UserRegisterReq;
import com.example.user.bizserver.service.impl.UserServiceImpl;
import com.example.user.infrastructure.entity.PointsLogDO;
import com.example.user.infrastructure.entity.UserAccountDO;
import com.example.user.infrastructure.entity.UserDO;
import com.example.user.infrastructure.mapper.PointsLogMapper;
import com.example.user.infrastructure.mapper.UserAccountMapper;
import com.example.user.infrastructure.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 * 覆盖：注册（用户名重复抛异常、成功赠送 100 积分写流水）
 *      积分扣减（余额不足返 false、成功写流水）
 *      积分返还（成功写流水）
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserAccountMapper userAccountMapper;
    @Mock
    private PointsLogMapper pointsLogMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterReq req;

    @BeforeEach
    void setUp() {
        req = new UserRegisterReq();
        req.setUsername("testuser");
        req.setPassword("123456");
        req.setNickname("测试");
    }

    /**
     * 用户名已存在 → 抛 BizException
     */
    @Test
    void register_usernameExists_throwsBizException() {
        // 模拟已存在同名用户
        when(userMapper.selectOne(any())).thenReturn(new UserDO());

        BizException ex = assertThrows(BizException.class, () -> userService.register(req));
        assertEquals(1002, ex.getCode());
        // 不应执行 insert
        verify(userMapper, never()).insert(any());
    }

    /**
     * 注册成功：创建用户 + 初始化积分账户 100 + 写流水 type=3
     */
    @Test
    void register_success_createsUserAndAccountAndPointsLog() {
        // 模拟用户名不存在
        when(userMapper.selectOne(any())).thenReturn(null);
        // 模拟 insert 后回填 id
        doAnswer(invocation -> {
            UserDO u = invocation.getArgument(0);
            u.setId(1L);
            return 1;
        }).when(userMapper).insert(any(UserDO.class));

        Long userId = userService.register(req);

        assertEquals(1L, userId);

        // 验证创建用户 role=0
        ArgumentCaptor<UserDO> userCaptor = ArgumentCaptor.forClass(UserDO.class);
        verify(userMapper).insert(userCaptor.capture());
        UserDO savedUser = userCaptor.getValue();
        assertEquals(0, savedUser.getRole());
        assertNotEquals("123456", savedUser.getPassword()); // 密码已 BCrypt 加密

        // 验证积分账户赠送 100
        ArgumentCaptor<UserAccountDO> accountCaptor = ArgumentCaptor.forClass(UserAccountDO.class);
        verify(userAccountMapper).insert(accountCaptor.capture());
        assertEquals(100, accountCaptor.getValue().getPoints());

        // 验证积分流水 type=3 注册赠送
        ArgumentCaptor<PointsLogDO> logCaptor = ArgumentCaptor.forClass(PointsLogDO.class);
        verify(pointsLogMapper).insert(logCaptor.capture());
        PointsLogDO log = logCaptor.getValue();
        assertEquals(100, log.getChange());
        assertEquals(3, log.getType());
    }

    /**
     * 扣减积分：余额不足 → 返回 false，不写流水
     */
    @Test
    void deductPoints_insufficientBalance_returnsFalse() {
        when(userAccountMapper.deductPoints(1L, 200)).thenReturn(0); // 受影响 0 行

        boolean result = userService.deductPoints(1L, 200);

        assertFalse(result);
        verify(pointsLogMapper, never()).insert(any());
    }

    /**
     * 扣减积分：余额充足 → 返回 true，写流水 type=2
     */
    @Test
    void deductPoints_sufficientBalance_returnsTrueAndWritesLog() {
        when(userAccountMapper.deductPoints(1L, 50)).thenReturn(1);
        UserAccountDO account = new UserAccountDO();
        account.setPoints(50); // 扣后余额
        when(userAccountMapper.selectOne(any())).thenReturn(account);

        boolean result = userService.deductPoints(1L, 50);

        assertTrue(result);
        ArgumentCaptor<PointsLogDO> logCaptor = ArgumentCaptor.forClass(PointsLogDO.class);
        verify(pointsLogMapper).insert(logCaptor.capture());
        assertEquals(-50, logCaptor.getValue().getChange());
        assertEquals(2, logCaptor.getValue().getType());
    }

    /**
     * 返还积分：成功 → 写流水 type=4
     */
    @Test
    void refundPoints_success_writesLog() {
        when(userAccountMapper.refundPoints(1L, 50)).thenReturn(1);
        UserAccountDO account = new UserAccountDO();
        account.setPoints(150); // 返还后余额
        when(userAccountMapper.selectOne(any())).thenReturn(account);

        boolean result = userService.refundPoints(1L, 50);

        assertTrue(result);
        ArgumentCaptor<PointsLogDO> logCaptor = ArgumentCaptor.forClass(PointsLogDO.class);
        verify(pointsLogMapper).insert(logCaptor.capture());
        assertEquals(50, logCaptor.getValue().getChange());
        assertEquals(4, logCaptor.getValue().getType());
    }
}
