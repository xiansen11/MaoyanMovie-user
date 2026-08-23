package com.example.user.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水表实体
 */
@Data
@TableName("t_points_log")
public class PointsLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Integer change;
    private Integer balance;

    /** 1 充值 / 2 下单消费 / 3 注册赠送 / 4 退票返还 */
    private Integer type;

    private String remark;
    private LocalDateTime createdAt;
}
