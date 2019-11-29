package csnight.redis.monitor.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseUtils {
    private static final String ip_part = "((25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
    private static final String port_part = "([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])";
    private static final String phone = "^[1][3,4,5,7,8,9][0-9]{9}$";
    private static final String email = "^[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";

    public static boolean any(List<Boolean> list) {
        if (list.isEmpty()) {
            return true;
        }
        boolean isSame = true;
        boolean temp_val = list.get(0);
        for (Boolean b : list) {
            if (temp_val != b) {
                isSame = false;
                break;
            }
        }
        return isSame;
    }

    public static String getResourceDir() {
        return Objects.requireNonNull(BaseUtils.class.getClassLoader().getResource("")).getPath();
    }

    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (!netInterface.isLoopback() && !netInterface.isVirtual() && netInterface.isUp()) {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return "";
    }

    public static String GetUserFromContext() {
        String username = "";
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            username = userDetails.getUsername();

        } catch (Exception ex) {
            username = "";
        }
        return username;
    }

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

    public static String bytesToBase64(byte[] blob, String ext) {
        String b64 = Base64.getEncoder().encodeToString(blob);
        switch (ext) {
            default:
            case "jpg":
                return "data:image/jpeg;base64," + b64;
            case "png":
                return "data:image/png;base64," + b64;
            case "gif":
                return "data:image/gif;base64," + b64;
        }
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
        StringBuffer hexValue = new StringBuffer();
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return prefix + hexValue.toString();

    }
}
