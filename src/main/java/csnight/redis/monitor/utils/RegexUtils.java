package csnight.redis.monitor.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    private static final String ip_part = "((25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
    private static final String port_part = "([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])";
    private static final String phone = "^[1][3,4,5,7,8,9][0-9]{9}$";
    private static final String email = "^[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";

    public static boolean checkPhone(String identify) {

        Pattern p = Pattern.compile(phone);
        Matcher m = p.matcher(identify);
        return m.matches();
    }

    public static boolean checkEmail(String identify) {

        Pattern p = Pattern.compile(email);
        Matcher m = p.matcher(identify);
        return m.matches();
    }

    public static boolean checkIpPort(String identify) {
        String pattern = "^" + ip_part + ":" + port_part + "$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(identify);
        return m.matches();
    }

    public static boolean checkIp(String identify) {
        Pattern r = Pattern.compile(ip_part);
        Matcher m = r.matcher(identify);
        return m.matches();
    }

    public static boolean checkPort(int identify) {
        Pattern r = Pattern.compile(port_part);
        Matcher m = r.matcher(String.valueOf(identify));
        return m.matches();
    }
}
