package csnight.redis.monitor.utils;

import java.util.Properties;

public class YamlConfigUtils {
    private static Properties ymlProperties = new Properties();

    public YamlConfigUtils(Properties properties) {
        ymlProperties = properties;
    }

    public static String getStrYmlVal(String key) {
        return ymlProperties.getProperty(key);
    }

    public static Integer getIntegerYmlVal(String key) {
        return Integer.valueOf(ymlProperties.getProperty(key));
    }

}