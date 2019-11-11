package com.csnight.redis.monitor.rest.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class MenuDto {
    private Long id;
    private boolean iframe;
    @NotEmpty
    @NotNull
    private String name;
    @NotEmpty
    @NotNull
    private String component;
    private Long pid;
    private Long sort;
    @NotEmpty
    @NotNull
    private String icon;
    @NotNull
    private String path;
    private boolean hidden;
    @NotEmpty
    @NotNull
    private String component_name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isIframe() {
        return iframe;
    }

    public void setIframe(boolean iframe) {
        this.iframe = iframe;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Long getSort() {
        return sort;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getComponent_name() {
        return component_name;
    }

    public void setComponent_name(String component_name) {
        this.component_name = component_name;
    }
}
