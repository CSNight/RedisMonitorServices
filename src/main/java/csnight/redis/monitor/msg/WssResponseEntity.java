package csnight.redis.monitor.msg;

import java.util.Date;

public class WssResponseEntity {
    private ResponseMsgType rmt;
    private Object body;
    private Long time = new Date().getTime();

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
}