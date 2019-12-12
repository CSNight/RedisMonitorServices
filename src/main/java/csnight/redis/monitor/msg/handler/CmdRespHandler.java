package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.BuilderFactory;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.msg.RedisCmdType;
import csnight.redis.monitor.msg.ResponseMsgType;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.GUID;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

public class CmdRespHandler implements WsChannelHandler {
    private RedisCmdType command;
    private byte[][] args;

    public boolean parseCmd(String cmd) {
        if (cmd != null && !cmd.equals("")) {
            String[] parts = cmd.split(" ");
            command = RedisCmdType.getEnumType(parts[0].toUpperCase());
            if (command.name().equals("UNKNOWN")) {
                return false;
            }
            if (parts.length > 1) {
                args = new byte[parts.length - 1][];
                for (int i = 1; i < parts.length; i++) {
                    args[i - 1] = parts[i].getBytes();
                }
            }
            return true;
        }
        return false;
    }

    public WssResponseEntity execute(JSONObject msg, Channel ch) {
        if (!parseCmd(msg.getString("msg"))) {
            return new WssResponseEntity(ResponseMsgType.Error, "Redis command parse error");
        }
        String ins = msg.getString("ins");
        if (ins == null || ins.equals("")) {
            return new WssResponseEntity(ResponseMsgType.Error, "Must specify a redis instance");
        }
        RedisPoolInstance rpi = MultiRedisPool.getInstance().getPool(ins);
        if (rpi == null) {
            return new WssResponseEntity(ResponseMsgType.Error, "Redis pool does not exist, please connect first");
        }
        try {
            String response;
            String jid = GUID.getUUID();
            JediSQL jedis = rpi.getJedis(jid);
            Object res = jedis.sendCommand(command, args);
            if (res instanceof byte[]) {
                response = new String((byte[]) res);
            } else if (res instanceof ArrayList) {
                List<String> tmp = BuilderFactory.STRING_LIST.build(res);
                response = JSONObject.toJSONString(tmp);
            } else {
                response = res.toString();
            }
            rpi.close(jid);
            return new WssResponseEntity(ResponseMsgType.RESP, response);
        } catch (Exception ex) {
            return new WssResponseEntity(ResponseMsgType.Error, ex.getMessage());
        }
    }
}
