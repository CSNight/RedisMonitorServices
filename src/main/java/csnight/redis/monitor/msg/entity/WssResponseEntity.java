package csnight.redis.monitor.msg.entity;

import csnight.redis.monitor.msg.series.ResponseMsgType;

import java.util.Date;

public class WssResponseEntity {
    private ResponseMsgType rmt;
    private Object body;
    private Long time = new Date().getTime();
    private Long cost = 0L;
    private String appId;

    public WssResponseEntity() {
    }

    public WssResponseEntity(ResponseMsgType rmt, Object body) {
        this.rmt = rmt;
        this.body = body;
    }

    public WssResponseEntity(ResponseMsgType rmt, Object body, Long cost) {
        this.rmt = rmt;
        this.body = body;
        this.cost = cost;
    }

    public ResponseMsgType getRmt() {
        return rmt;
    }

    public void setRmt(ResponseMsgType rmt) {
        this.rmt = rmt;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
