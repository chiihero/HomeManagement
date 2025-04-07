package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// 添加类级Swagger注解
@Schema(description = "用户实体类")
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID", required = true)
    private Long id;

    @Schema(description = "用户名", required = true, example = "admin")
    private String username;

    @Schema(description = "密码", required = true, example = "$2a$10$...")
    private String password;

    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    // 修改role字段注解
    @Schema(description = "角色：ADMIN-管理员，USER-普通用户", allowableValues = {"ADMIN", "USER"}, defaultValue = "USER")
    private String role;

    // 修改status字段注解
    @Schema(description = "状态: active-活跃, locked-锁定, disabled-禁用", defaultValue = "active")
    private String status;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    private LocalDateTime updateTime;
}