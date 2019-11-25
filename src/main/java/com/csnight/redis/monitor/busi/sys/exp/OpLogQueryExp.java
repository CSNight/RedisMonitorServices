package com.csnight.redis.monitor.busi.sys.exp;

import com.csnight.redis.monitor.db.blurry.Query;

import java.util.Date;

public class OpLogQueryExp {
    @Query(type = Query.Type.EQUAL)
    private String un;
    @Query(type = Query.Type.INNER_LIKE)
    private String op;
    @Query(type = Query.Type.EQUAL)
    private String mo;
    @Query(type = Query.Type.INNER_LIKE)
    private String ip;
    @Query(type = Query.Type.GREATER_THAN, propName = "ct")
    private Date st;
    @Query(type = Query.Type.LESS_THAN, propName = "ct")
    private Date et;

    private String sort;
    private String direct;
    private int cur;
    private int size;

    public String getUn() {
        return un;
    }

    public void setUn(String un) {
        this.un = un;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getMo() {
        return mo;
    }

    public void setMo(String mo) {
        this.mo = mo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getSt() {
        return st;
    }

    public void setSt(Date st) {
        this.st = st;
    }

    public Date getEt() {
        return et;
    }

    public void setEt(Date et) {
        this.et = et;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDirect() {
        return direct;
    }

    public void setDirect(String direct) {
        this.direct = direct;
    }

    public int getCur() {
        return cur;
    }

    public void setCur(int cur) {
        this.cur = cur;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
