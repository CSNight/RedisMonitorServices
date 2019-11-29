package csnight.redis.monitor.redis.pool;

import com.csnight.jedisql.JedisPoolConfig;
import csnight.redis.monitor.utils.BaseUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PoolConfig {
    private String ins_id = "";
    private String user_id = "";
    private String ip = "";
    private String uin = "";
    private int port = 6379;
    private String poolType = "sin";
    private String password = null;
    private int db = 0;
    private boolean blockWhenExhausted = true;
    private String evictionPolicy = "org.apache.commons.pool2.impl.DefaultEvictionPolicy";
    private boolean jmx = true;
    private int totalCon = 100;
    private int maxWait = 1000 * 10;
    private int timeOut = 2000;
    private boolean testOnBorrow = true;
    private String master = "";
    private Set<String> sentinels = new HashSet<>();

    public String getIns_id() {
        return ins_id;
    }

    public void setIns_id(String ins_id) {
        this.ins_id = ins_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public String getPoolType() {
        return poolType;
    }

    public void setPoolType(String poolType) {
        this.poolType = poolType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
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

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public String getUin() {
        return uin;
    }

    public boolean checkSentinelsConfig() {
        return !master.equals("") && sentinels.size() > 0
                && BaseUtils.any(sentinels.stream().map(BaseUtils::checkIpPort).collect(Collectors.toList()));
    }

    public JedisPoolConfig BuildJedisConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        // 连接耗尽时是否阻塞, false报异常,true阻塞直到超时, 默认true
        config.setBlockWhenExhausted(this.blockWhenExhausted);
        // 设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
        config.setEvictionPolicyClassName(this.evictionPolicy);
        // 是否启用pool的jmx管理功能, 默认true
        config.setJmxEnabled(this.jmx);
        // 最大连接数, 默认8个
        config.setMaxTotal(this.totalCon);
        // 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        config.setMaxWaitMillis(this.maxWait);
        // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        config.setTestOnBorrow(this.testOnBorrow);

        return config;
    }

    public void checkMd5() {
        if (this.ip != null && BaseUtils.checkIp(this.ip) & BaseUtils.checkPort(this.port)) {
            this.uin = BaseUtils.string2MD5(this.user_id + ":" + this.ip + ":" + this.port, "INS$");
        } else if (checkSentinelsConfig()) {
            StringBuilder sen = new StringBuilder();
            for (String sentinel : sentinels) {
                sen.append(sentinel).append(",");
            }
            String body = this.user_id + ":" + this.master + ":" + sen;
            this.uin = BaseUtils.string2MD5(body, "INS$");
        }
    }
}
