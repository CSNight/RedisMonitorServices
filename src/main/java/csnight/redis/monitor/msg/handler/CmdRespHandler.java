package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.exception.CmdMsgException;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.redis.data.ResultParser;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.IdentifyUtils;

import java.util.LinkedList;
import java.util.List;

public class CmdRespHandler implements WsChannelHandler {
    private RedisCmdType command;
    private byte[][] args;
    private RedisPoolInstance rpi;
    private JediSQL jedis;
    private String jid = IdentifyUtils.getUUID();
    private String ins;

    @Override
    public String getIns() {
        return ins;
    }

    @Override
    public void initialize(JSONObject msg) throws CmdMsgException {
        if (!parseCmd(msg.getString("msg"))) {
            throw new CmdMsgException("Redis command parse error");
        }
        ins = msg.getString("ins");
        if (ins == null || ins.equals("")) {
            throw new CmdMsgException("Must specify a redis instance");
        }
        rpi = MultiRedisPool.getInstance().getPool(ins);
        if (rpi == null) {
            throw new CmdMsgException("Redis pool does not exist, please connect first");
        }
    }

    /**
     * 功能描述: 命令解析
     *
     * @param cmd 命令
     * @return : boolean
     * @author csnight
     * @since 2019/12/27 8:52
     */
    public boolean parseCmd(String cmd) {
        if (cmd != null && !cmd.equals("")) {
            String[] parts = cmd.split(" ");
            List<String> ps = new LinkedList<>();
            for (String p : parts) {
                if (!p.equals("")) {
                    ps.add(p);
                }
            }
            parts = ps.toArray(new String[]{});
            ps.clear();
            ps = null;
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
            Object response = ResultParser.MsgParser(res);
            return new WssResponseEntity(ResponseMsgType.RESP, response, end);
        } catch (Exception ex) {
            return new WssResponseEntity(ResponseMsgType.ERROR, ex.getMessage() == null ? "Null response exception" : ex.getMessage());
        } finally {
            rpi.close(jid);
        }
    }

    @Override
    public void destroy() {
        rpi.close(jid);
        jedis = null;
    }
}
