package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.exception.CmdMsgException;

public interface WsChannelHandler {
    String getIns();

    void initialize(JSONObject msg) throws CmdMsgException;

    void destroy();
}
