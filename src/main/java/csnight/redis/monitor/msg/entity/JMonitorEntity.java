package csnight.redis.monitor.msg.entity;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import com.csnight.jedisql.JedisMonitor;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

public class JMonitorEntity extends JedisMonitor {
    private Channel ch;
    private String id = IdentifyUtils.getUUID();
    private JediSQL jediSQL;
    private String appId;

    public void setCh(Channel ch) {
        this.ch = ch;
    }

    public String getId() {
        return id;
    }

    public JediSQL getJediSQL() {
        return jediSQL;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setJediSQL(JediSQL jediSQL) {
        this.jediSQL = jediSQL;
    }

    @Override
    public void onCommand(String s) {
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.MONITOR, s);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
    }

    public void jmonitor() {
        jediSQL.monitor(this);
    }

    public void junmonitor() {
        jediSQL.unmonitor(this);
    }
}
