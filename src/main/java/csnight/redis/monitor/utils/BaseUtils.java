package csnight.redis.monitor.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class BaseUtils {

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
}
