package com.csnight.redis.monitor.utils;

import io.swagger.annotations.ApiModel;

import java.util.Date;

@ApiModel
public class RespTemplate {
    private Date ack = new Date();
    private int status;
    private Object message;
    private String uri;
    private String method;

    public RespTemplate(int status, Object msg, String uri, String method) {
        this.status = status;
        this.message = msg;
        this.uri = uri;
        this.method = method;
    }

    public Date getAck() {
        return ack;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
