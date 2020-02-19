package csnight.redis.monitor.busi.sys;

import com.sun.mail.util.MailSSLSocketFactory;
import csnight.redis.monitor.db.jpa.SysMailConfig;
import csnight.redis.monitor.db.repos.SysMailConfRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.rest.sys.dto.MailConfDto;
import csnight.redis.monitor.rest.sys.dto.MailSendDto;
import csnight.redis.monitor.utils.BaseUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;

@Service
public class MailSendService {
    @Resource
    private SysUserRepository userRepository;
    @Resource
    private SysMailConfRepository mailConfRepository;

    public List<SysMailConfig> GetAllMailConfig() {
        return mailConfRepository.findAll();
    }

    public SysMailConfig GetUserMailConfig() {
        String uid = userRepository.findIdByUsername(BaseUtils.GetUserFromContext());
        return mailConfRepository.findByUid(uid);
    }

    public SysMailConfig UpdateMailConfig(MailConfDto dto) {
        String uid = userRepository.findIdByUsername(BaseUtils.GetUserFromContext());
        SysMailConfig mailConfig = mailConfRepository.findByUid(uid);
        if (mailConfig == null) {
            mailConfig = new SysMailConfig();
        }
        mailConfig.setEmail(dto.getEmail());
        mailConfig.setHost(dto.getHost());
        mailConfig.setPort(dto.getPort());
        mailConfig.setUsername(dto.getUsername());
        mailConfig.setPwd(dto.getPwd());
        mailConfig.setUid(uid);
        mailConfig.setProtocol("smtp");
        mailConfig.setEncode(dto.getEncode());
        return mailConfRepository.save(mailConfig);
    }

    public String DeleteMailConfig() {
        String uid = userRepository.findIdByUsername(BaseUtils.GetUserFromContext());
        SysMailConfig mailConfig = mailConfRepository.findByUid(uid);
        if (mailConfig != null) {
            mailConfRepository.deleteById(mailConfig.getId());
            return "success";
        }
        return "failed";
    }

    public String SendMail(MailSendDto dto) {
        String uid = userRepository.findIdByUsername(BaseUtils.GetUserFromContext());
        SysMailConfig mailConfig = mailConfRepository.findByUid(uid);
        if (mailConfig != null) {
            try {
                Session session = initProperties(mailConfig);
                JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
                mailSender.setSession(session);
                MimeMessage mailMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true,mailConfig.getEncode());
                helper.setValidateAddresses(true);
                helper.setFrom(mailConfig.getEmail());
                helper.setTo(dto.getToList().toArray(new String[]{}));
                helper.setSubject(dto.getSubject());
                helper.setText("", dto.getContent());
                mailSender.send(mailMessage);
                return "success";
            } catch (MessagingException e) {
                return e.getMessage();
            }
        }
        return "config not found";
    }

    private Session initProperties(SysMailConfig conf) {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", conf.getProtocol());
        properties.setProperty("mail.smtp.host", conf.getHost());
        properties.setProperty("mail.smtp.port", String.valueOf(conf.getPort()));
        // 使用smtp身份验证
        properties.put("mail.smtp.auth", "true");
        // 使用SSL,企业邮箱必需 start
        // 开启安全协议
        MailSSLSocketFactory mailSSLSocketFactory = null;
        try {
            mailSSLSocketFactory = new MailSSLSocketFactory();
            mailSSLSocketFactory.setTrustAllHosts(true);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        properties.put("mail.smtp.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", mailSSLSocketFactory);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.smtp.socketFactory.port", String.valueOf(conf.getPort()));
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(conf.getEmail(), conf.getPwd());
            }
        });
        // 使用SSL,企业邮箱必需 end
        // TODO 显示debug信息 正式环境注释掉
        session.setDebug(false);
        return session;
    }
}
