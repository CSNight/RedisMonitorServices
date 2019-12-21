package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.exception.CmdMsgException;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.IdentifyUtils;

import java.util.ArrayList;
import java.util.List;

public class CmdRespHandler implements WsChannelHandler {
    private RedisCmdType command;
    private byte[][] args;
    private RedisPoolInstance rpi;
    private JediSQL jedis;
    private String jid = IdentifyUtils.getUUID();

    @Override
    public void initialize(JSONObject msg) throws CmdMsgException {
        if (!parseCmd(msg.getString("msg"))) {
            throw new CmdMsgException("Redis command parse error");
        }
        String ins = msg.getString("ins");
        if (ins == null || ins.equals("")) {
            throw new CmdMsgException("Must specify a redis instance");
        }
        rpi = MultiRedisPool.getInstance().getPool(ins);
        if (rpi == null) {
            throw new CmdMsgException("Redis pool does not exist, please connect first");
        }
    }

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

    public WssResponseEntity execute() {

        try {
            jedis = rpi.getJedis(jid);
            long start = System.currentTimeMillis();
            Object res = jedis.sendCommand(command, args);
            long end = System.currentTimeMillis() - start;
            Object response = MsgParser(res);
            return new WssResponseEntity(ResponseMsgType.RESP, response, end);
        } catch (Exception ex) {
            return new WssResponseEntity(ResponseMsgType.ERROR, ex.getMessage() == null ? "Null response exception" : ex.getMessage());
        } finally {
            rpi.close(jid);
        }
    }

    private Object MsgParser(Object res) {
        Object response;
        if (res instanceof byte[]) {
            response = new String((byte[]) res);
        } else if (res instanceof ArrayList) {
            response = ArrayMsgParser(res);
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
                List<Object> recursive = ArrayMsgParser(item);
                tmp.add(recursive);
            } else {
                tmp.add(item);
            }
        }
        return tmp;
    }


    @Override
    public void destroy() {
        rpi.close(jid);
        jedis = null;
    }
}
