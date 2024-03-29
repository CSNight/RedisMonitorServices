package csnight.redis.monitor.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常工具
 *
 * @author Zheng Jie
 * @date 2019-01-06
 */
public class ThrowableUtils {

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }
}
