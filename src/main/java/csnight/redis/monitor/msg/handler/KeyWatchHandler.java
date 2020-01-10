package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.exception.CmdMsgException;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.quartz.MsCustomTask;
import csnight.redis.monitor.redis.data.KeyOperator;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.rest.rms.dto.KeyEntDto;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

public class KeyWatchHandler implements WsChannelHandler {
    private RedisPoolInstance rpi;
    private String appId;
    private Channel channel;
    private String ins;
    private KeyOperator keyOperator = new KeyOperator();
    private MsCustomTask customTask = new MsCustomTask();

    public KeyWatchHandler(String appId, Channel ch) {
        this.appId = appId;
        this.channel = ch;
    }

    @Override
    public String getIns() {
        return ins;
    }

    @Override
    public void initialize(JSONObject msg) throws CmdMsgException {
        ins = msg.getString("ins");
        if (ins == null || ins.equals("")) {
            throw new CmdMsgException("Must specify a redis instance");
        }
        rpi = MultiRedisPool.getInstance().getPool(ins);
        if (rpi == null) {
            throw new CmdMsgException("Redis pool does not exist, please connect first");
        }
        JSONObject jo = JSONObject.parseObject(msg.getString("msg"));
        long delay = jo.getLongValue("delay");
        long interval = jo.getLongValue("interval");
        KeyEntDto dto = JSONObject.parseObject(msg.getString("msg"), KeyEntDto.class);
        customTask.StartCustomTaskPool(() -> {
            WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.KEYWATCH, keyOperator.GetKeyValue(rpi, dto));
            wre.setAppId(appId);
            WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
        }, delay, interval);
    }

    @Override
    public void destroy() {
        customTask.StopCustomTaskPool();
    }
}
