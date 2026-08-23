package com.example.user.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表实体
 */
@Data
@TableName("t_user")
public class UserDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;

    /** 0 普通用户 / 1 平台管理员 / 2 影院工作人员 */
    private Integer role;

    /** 影院工作人员所属影院（role=2 时必填） */
    private Long cinemaId;

    /** 0 正常 / 1 禁用 */
    private Integer status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
