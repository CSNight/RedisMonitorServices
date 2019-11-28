package csnight.redis.monitor.utils;


import java.util.UUID;

public class GUID {
    /**
     * 生成UUID
     *
     * @return string
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成UUID，但会过滤-
     *
     * @return string
     */
    public static String getUUID2() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "").toUpperCase();
    }
}
