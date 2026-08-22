package com.example.user.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建影院工作人员账号请求（影院审核通过后由 movie-server 调用）
 */
@Data
public class CinemaAccountReq {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "影院 ID 不能为空")
    private Long cinemaId;

    private String nickname;
}
