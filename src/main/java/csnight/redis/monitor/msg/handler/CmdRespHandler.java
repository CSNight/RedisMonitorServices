package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.BuilderFactory;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.IdentifyUtils;
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
            } else {
                args = new byte[][]{};
            }
            return true;
        }
        return false;
    }

    public WssResponseEntity execute(JSONObject msg, Channel ch) {
        if (!parseCmd(msg.getString("msg"))) {
            return new WssResponseEntity(ResponseMsgType.ERROR, "Redis command parse error");
        }
        String ins = msg.getString("ins");
        if (ins == null || ins.equals("")) {
            return new WssResponseEntity(ResponseMsgType.ERROR, "Must specify a redis instance");
        }
        RedisPoolInstance rpi = MultiRedisPool.getInstance().getPool(ins);
        if (rpi == null) {
            return new WssResponseEntity(ResponseMsgType.ERROR, "Redis pool does not exist, please connect first");
        }
        try {
            String jid = IdentifyUtils.getUUID();
            JediSQL jedis = rpi.getJedis(jid);
            long start = System.currentTimeMillis();
            Object res = jedis.sendCommand(command, args);
            long end = System.currentTimeMillis() - start;
            String response = MsgParser(res);
            rpi.close(jid);
            return new WssResponseEntity(ResponseMsgType.RESP, response, end);
        } catch (Exception ex) {
            return new WssResponseEntity(ResponseMsgType.ERROR, ex.getMessage());
        }
    }

    private String MsgParser(Object res) {
        String response;
        if (res instanceof byte[]) {
            response = new String((byte[]) res);
        } else if (res instanceof ArrayList) {
            response = JSONObject.toJSONString(ArrayMsgParser(res));
        } else {
            response = res.toString();
        }
        return response;
    }

    private List<Object> ArrayMsgParser(Object res) {
        ArrayList<Object> resp = (ArrayList) res;
        List<Object> tmp = new ArrayList<>();
        for (Object item : resp) {
            if (item instanceof byte[]) {
                tmp.add(new String((byte[]) item));
            } else if (item instanceof ArrayList) {
                ArrayList<Object> itemSub = (ArrayList) item;
                if (itemSub.size() > 0 && itemSub.get(0) instanceof byte[]) {
                    List<String> subItems = BuilderFactory.STRING_LIST.build(item);
                    tmp.add(subItems);
                } else {
                    List<Object> recursive = ArrayMsgParser(itemSub);
                    tmp.add(recursive);
                }
            } else {
                tmp.add(item);
            }
        }
        return tmp;
    }
}
