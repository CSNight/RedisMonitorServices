package csnight.redis.monitor.utils;


import java.security.MessageDigest;
import java.util.UUID;

public class IdentifyUtils {
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

    /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr, String prefix) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return prefix + hexValue.toString();

    }
}
