package csnight.redis.monitor.utils;

import csnight.redis.monitor.db.jpa.SysRole;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysUserRepository;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

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
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
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

    public static Set<String> GetUserAuthorities(String user_id) {
        Set<String> authorities;
        try {
            SysUser user = ReflectUtils.getBean(SysUserRepository.class).findOnly(user_id);
            Collection<? extends GrantedAuthority> userAuthorities = user.getAuthorities();
            authorities = AuthorityUtils.authorityListToSet(userAuthorities);
        } catch (Exception ex) {
            authorities = new HashSet<>();
        }
        return authorities;
    }

    public static Set<String> GetUserCmdAuth(String user_id) {
        Set<String> cmdAuth = new HashSet<>();
        try {
            SysUser user = ReflectUtils.getBean(SysUserRepository.class).findOnly(user_id);
            if (user_id != null) {
                for (SysRole role : user.getRoles()) {
                    String[] commands = role.getCommands().getCommands().split(",");
                    cmdAuth.addAll(Arrays.asList(commands));
                }
            }
        } catch (Exception ex) {
            cmdAuth = new HashSet<>();
        }
        return cmdAuth;
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

    public static String getEncoding(byte[] bytes) {
        String DEFAULT_ENCODING = "UTF-8";
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        return encoding;
    }
}
