package com.example.user.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分账户表实体
 */
@Data
@TableName("t_user_account")
public class UserAccountDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Integer points;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    private LocalDateTime updatedAt;
}
