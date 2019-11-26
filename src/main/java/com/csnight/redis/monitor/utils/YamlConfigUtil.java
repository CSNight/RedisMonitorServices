package com.csnight.redis.monitor.utils;

import java.util.Properties;

public class YamlConfigUtil {
    private static Properties ymlProperties = new Properties();

    public YamlConfigUtil(Properties properties) {
        ymlProperties = properties;
    }

    public static String getStrYmlVal(String key) {
        return ymlProperties.getProperty(key);
    }

    public static Integer getIntegerYmlVal(String key) {
        return Integer.valueOf(ymlProperties.getProperty(key));
    }

}
