package csnight.redis.monitor.rest.rms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "键实体模型")
public class KeyEntDto {
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "实例ID", required = true)
    private String ins_id;
    @Min(0)
    @ApiModelProperty(notes = "数据库索引", example = "0")
    private int db;
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "键名称", required = true)
    private String keyName;
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "键类型", required = true)
    private String type;
    @ApiModelProperty(notes = "键大小", example = "0")
    @Min(0)
    private long size;
    @Min(-1)
    @ApiModelProperty(notes = "键过期时长", example = "-1")
    private long ttl = -1;
    @ApiModelProperty(notes = "键列表")
    private List<String> keys = new ArrayList<>();

    public String getIns_id() {
        return ins_id;
    }

    public void setIns_id(String ins_id) {
        this.ins_id = ins_id;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}
