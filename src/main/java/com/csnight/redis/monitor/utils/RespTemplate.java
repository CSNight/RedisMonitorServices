package com.csnight.redis.monitor.utils;

import io.swagger.annotations.ApiModel;
import org.springframework.http.HttpStatus;

import java.util.Date;

@ApiModel(value = "请求响应模板")
public class RespTemplate {
    private Date ack = new Date();
    private int status;
    private HttpStatus code;
    private Object message;
    private String uri;
    private String method;

    public RespTemplate(HttpStatus code, Object msg) {
        this.code = code;
        this.message = msg;
        if (message == null) {
            message = "failed";
        }
    }

    public RespTemplate(int status, HttpStatus code, Object msg, String uri, String method) {
        this.status = status;
        this.message = msg;
        this.uri = uri;
        this.method = method;
        this.code = code;
        if (code.equals(HttpStatus.OK)) {
            this.status = 200;
        }
        if (message == null) {
            message = "failed";
        }
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

    public void setAck(Date ack) {
        this.ack = ack;
    }

    public HttpStatus getCode() {
        return code;
    }

    public void setCode(HttpStatus code) {
        this.code = code;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
