package com.redis.monitor.db.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "rms_instance")
public class RmsInstance {
    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 50)
    private String id;
    @Column(name = "user_id", length = 50)
    private String user_id;
    @Column(name = "ip")
    private String ip;
    @Column(name = "port")
    private int port;
    @Column(name = "instance_name")
    private String instance_name;
    @Column(name = "cluster_enable")
    private boolean cluster_enable;
    @Column(name = "role")
    private String role;
    @Column(name = "version")
    private String version;
    @Column(name = "mode")
    private String mode;
    @Column(name = "os")
    private String os;
    @Column(name = "arch_bits")
    private int arch_bits;
    @Column(name = "proc_id")
    private int proc_id;
    @Column(name = "uptime_in_seconds")
    private long uptime_in_seconds;
    @Column(name = "exec")
    private String exec;
    @Column(name = "config")
    private String config;
    @Column(name = "conn")
    private String conn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getInstance_name() {
        return instance_name;
    }

    public void setInstance_name(String instance_name) {
        this.instance_name = instance_name;
    }

    public boolean isCluster_enable() {
        return cluster_enable;
    }

    public void setCluster_enable(boolean cluster_enable) {
        this.cluster_enable = cluster_enable;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public int getArch_bits() {
        return arch_bits;
    }

    public void setArch_bits(int arch_bits) {
        this.arch_bits = arch_bits;
    }

    public int getProc_id() {
        return proc_id;
    }

    public void setProc_id(int proc_id) {
        this.proc_id = proc_id;
    }

    public long getUptime_in_seconds() {
        return uptime_in_seconds;
    }

    public void setUptime_in_seconds(long uptime_in_seconds) {
        this.uptime_in_seconds = uptime_in_seconds;
    }

    public String getExec() {
        return exec;
    }

    public void setExec(String exec) {
        this.exec = exec;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getConn() {
        return conn;
    }

    public void setConn(String conn) {
        this.conn = conn;
    }
}
