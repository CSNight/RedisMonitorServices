package csnight.redis.monitor.rest.rms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "Redis实例模型")
public class RmsInsDto {
    @ApiModelProperty(notes = "实例ID")
    private String id;
    @ApiModelProperty(notes = "IP")
    private String ip;
    @Max(value = 65535)
    @Min(value = 0)
    @ApiModelProperty(notes = "端口", example = "6379")
    private int port = 6379;
    @NotEmpty
    @NotNull
    @ApiModelProperty(notes = "实例名称", required = true)
    private String name;
    @ApiModelProperty(notes = "实例密码")
    private String password;
    @ApiModelProperty(notes = "实例主机名")
    private String master;
    @ApiModelProperty(notes = "实例哨兵集")
    private Set<String> sentinels = new HashSet<>();
    @ApiModelProperty(notes = "实例连接池类型")
    private String poolType = "sin";
    @ApiModelProperty(notes = "实例状态", example = "true")
    private boolean state;
    @Max(value = 15)
    @Min(value = 0)
    @ApiModelProperty(notes = "实例连接DB索引", example = "0")
    private int db = 0;
    @ApiModelProperty(notes = "连接池忙时阻塞", example = "true")
    private boolean blockWhenExhausted = true;
    @ApiModelProperty(notes = "连接池淘汰策略")
    private String evictionPolicy = "org.apache.commons.pool2.impl.DefaultEvictionPolicy";
    @ApiModelProperty(notes = "启用JMX", example = "true")
    private boolean jmx = true;
    @ApiModelProperty(notes = "连接最大数量", example = "100")
    private int totalCon = 100;
    @ApiModelProperty(notes = "连接最大等待", example = "10000")
    private int maxWait = 1000 * 10;
    @ApiModelProperty(notes = "连接超时时长", example = "v")
    private int timeOut = 2000;
    @ApiModelProperty(notes = "启用连接借用测试", example = "true")
    private boolean testOnBorrow = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public Set<String> getSentinels() {
        return sentinels;
    }

    public void setSentinels(Set<String> sentinels) {
        this.sentinels = sentinels;
    }

    public String getPoolType() {
        return poolType;
    }

    public void setPoolType(String poolType) {
        this.poolType = poolType;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public boolean isBlockWhenExhausted() {
        return blockWhenExhausted;
    }

    public void setBlockWhenExhausted(boolean blockWhenExhausted) {
        this.blockWhenExhausted = blockWhenExhausted;
    }

    public String getEvictionPolicy() {
        return evictionPolicy;
    }

    public void setEvictionPolicy(String evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    public boolean isJmx() {
        return jmx;
    }

    public void setJmx(boolean jmx) {
        this.jmx = jmx;
    }

    public int getTotalCon() {
        return totalCon;
    }

    public void setTotalCon(int totalCon) {
        this.totalCon = totalCon;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }
}
