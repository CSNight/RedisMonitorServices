package com.csnight.redis.monitor.db.jpa;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_operation_log")
public class SysOpLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private String id;
    @Column(name = "username")
    private String username;
    @Column(name = "ct")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ct;
    @Column(name = "level")
    private String level;
    @Column(name = "operation")
    private String operation;
    @Column(name = "tn")
    private String tn;
    @Column(name = "proc")
    private String proc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCt() {
        return ct;
    }

    public void setCt(Date ct) {
        this.ct = ct;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTn() {
        return tn;
    }

    public void setTn(String tn) {
        this.tn = tn;
    }

    public String getProc() {
        return proc;
    }

    public void setProc(String proc) {
        this.proc = proc;
    }
}
