package csnight.redis.monitor.redis.data;

import csnight.redis.monitor.utils.BaseUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ResultParser {
    public static Object MsgParser(Object res) {
        Object response;
        if (res instanceof byte[]) {
            String charset = BaseUtils.getEncoding((byte[]) res);
            if (!charset.toLowerCase().contains("gb")) {
                charset = "utf-8";
            }
            response = new String((byte[]) res, Charset.forName(charset));
        } else if (res instanceof ArrayList) {
            response = ArrayMsgParser(res);
        } else {
            response = res.toString();
        }
        return response;
    }

    /**
     * 功能描述: 消息递归解析
     *
     * @param res 消息体
     * @return : java.util.List<java.lang.Object>
     * @author csnight
     * @since 2019/12/27 8:53
     */
    private static List<Object> ArrayMsgParser(Object res) {
        ArrayList<Object> resp = (ArrayList) res;
        List<Object> tmp = new ArrayList<>();
        for (Object item : resp) {
            if (item instanceof byte[]) {
                String charset = BaseUtils.getEncoding((byte[]) item);
                if (!charset.toLowerCase().contains("gb")) {
                    charset = "utf-8";
                }
                tmp.add(new String((byte[]) item, Charset.forName(charset)));
            } else if (item instanceof ArrayList) {
                List<Object> recursive = ArrayMsgParser(item);
                tmp.add(recursive);
            } else {
                tmp.add(item);
            }
        }
        return tmp;
    }
}
