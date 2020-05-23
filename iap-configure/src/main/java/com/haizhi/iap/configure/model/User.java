package com.haizhi.iap.configure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Data;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by chenbo on 17/4/6.
 */
@Data
public class User {
    public enum SysRole{
        ROLE_USER, ROLE_ADMIN,ROLE_SUPER
    }

    private Long id;           // 用户id
    private String username;       // 登录账号名称
    private String password;
    private String salt;
    private String mobile;         // 手机号
    private List<String> authorities; //根据permission而来,费数据库字段

    @JsonProperty("token_dead_time")
    private Long tokenDeadTime;

    private String permission;

    private Integer role;      // 系统角色[0 普通用户，1管理员，2超级管理员].根据permission而来,费数据库字段

    private boolean admin;     // 是否管理员

    @JsonProperty("create_time")
    private Date createTime;

    @JsonProperty("update_time")
    private Date updateTime;

    public Integer getRole(){
        return getRole(this.permission);
    }

    public static Integer getRole(String permission) {
        if(!Strings.isNullOrEmpty(permission)) {
            if (permission.contains("ROLE_SUPER")) {
                return SysRole.ROLE_SUPER.ordinal();
            } else if (permission.contains("ROLE_ADMIN")) {
                return SysRole.ROLE_ADMIN.ordinal();
            } else if (permission.contains("ROLE_USER")) {
                return SysRole.ROLE_USER.ordinal();
            }
        }

        return 0;
    }

    public void setRole(Integer role) {
        this.role = role;

        if(this.role == SysRole.ROLE_SUPER.ordinal()){
            setPermission("ROLE_USER,ROLE_ADMIN,ROLE_SUPER");
        } else if(this.role == SysRole.ROLE_ADMIN.ordinal()){
            setPermission("ROLE_USER,ROLE_ADMIN");
        } else {
            setPermission("ROLE_USER");
        }
    }

    public void setPermission(Integer role){
        if(this.role == SysRole.ROLE_SUPER.ordinal()){
            setPermission("ROLE_USER,ROLE_ADMIN,ROLE_SUPER");
        } else if(this.role == SysRole.ROLE_ADMIN.ordinal()){
            setPermission("ROLE_USER,ROLE_ADMIN");
        } else {
            setPermission("ROLE_USER");
        }
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setPermissionFields(){
        if (!Strings.isNullOrEmpty(permission)) {
            String[] permissions = this.permission.split(",");

            setAuthorities(Arrays.asList(permissions));
            setRole(User.getRole(this.permission));

            if(this.permission.contains("ROLE_SUPER") || this.permission.contains("ROLE_ADMIN")){
                setAdmin(true);
            }
        }
    }

    public List<String> getAuthorities(){
        String[] permissions = this.permission.split(",");
        return Arrays.asList(permissions);
    }

    public void buildExtData(){
        if(this.admin){
            setPermission("ROLE_USER,ROLE_ADMIN");
        } else {
            setPermission("ROLE_USER");
        }
    }

    public boolean getAdmin(){
        if(this.permission.contains("ROLE_SUPER") || this.permission.contains("ROLE_ADMIN")){
            this.admin = true;
        }else {
            this.admin = false;
        }
        return this.admin;
    }

    public void mask(){
        this.setPassword("");
    }
}
