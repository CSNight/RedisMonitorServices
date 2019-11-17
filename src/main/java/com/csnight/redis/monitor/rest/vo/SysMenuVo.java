package com.csnight.redis.monitor.rest.vo;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "角色菜单更新模型")
public class SysMenuVo {
    private Long id;

    private Long pid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }
}
