package csnight.redis.monitor.rest.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "菜单模型")
public class MenuDto {
    @ApiModelProperty(notes = "菜单ID", example = "0")
    private Long id;
    @ApiModelProperty(notes = "是否为外部链接", example = "false")
    private boolean iframe;
    @NotEmpty
    @NotNull
    @ApiModelProperty(notes = "菜单名称", required = true)
    private String name;
    @NotEmpty
    @NotNull
    @ApiModelProperty(notes = "组件", required = true)
    private String component;
    @ApiModelProperty(notes = "父目录id", example = "0")
    private Long pid;
    @ApiModelProperty(notes = "排序索引", example = "0")
    private int sort;
    @NotEmpty
    @NotNull
    @ApiModelProperty(notes = "图标", required = true)
    private String icon;
    @NotNull
    @ApiModelProperty(notes = "路径")
    private String path;
    @ApiModelProperty(notes = "是否隐藏", example = "false")
    private boolean hidden;
    @NotEmpty
    @NotNull
    @ApiModelProperty(notes = "组件名称", required = true)
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

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
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
