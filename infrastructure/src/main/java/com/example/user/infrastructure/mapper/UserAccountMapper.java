package com.example.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.user.infrastructure.entity.UserAccountDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 积分账户 Mapper
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccountDO> {

    /**
     * 条件扣减积分（余额 >= ? 才扣成功）
     * 受影响行数 0 表示余额不足
     */
    @Update("UPDATE t_user_account SET points = points - #{points}, version = version + 1, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND points >= #{points}")
    int deductPoints(@Param("userId") Long userId, @Param("points") int points);

    /**
     * 返还积分
     */
    @Update("UPDATE t_user_account SET points = points + #{points}, version = version + 1, updated_at = NOW() " +
            "WHERE user_id = #{userId}")
    int refundPoints(@Param("userId") Long userId, @Param("points") int points);
}
