package csnight.redis.monitor.busi.sys;

import com.sun.mail.util.MailSSLSocketFactory;
import csnight.redis.monitor.db.jpa.SysMailConfig;
import csnight.redis.monitor.db.jpa.SysMailRecord;
import csnight.redis.monitor.db.repos.SysMailConfRepository;
import csnight.redis.monitor.db.repos.SysMailRecRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.rest.sys.dto.MailConfDto;
import csnight.redis.monitor.rest.sys.dto.MailSendDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.KeyManagerFactory;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service

public class MailSendService {
    @Resource
    private SysUserRepository userRepository;
    @Resource
    private SysMailConfRepository mailConfRepository;
    @Resource
    private SysMailRecRepository mailRecRepository;
    @Resource
    private KeyManagerFactory kmf;

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
        if (dto.getReceivers().size() > 0) {
            mailConfig.setContact(String.join(";", dto.getReceivers().toArray(new String[]{})));
        } else {
            mailConfig.setContact("");
        }
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
            SysMailRecord record = new SysMailRecord();
            record.setEmail(mailConfig.getEmail());
            record.setFlag(IdentifyUtils.string2MD5(dto.getContent(), ""));
            record.setTheme(dto.getSubject());
            record.setTos(String.join(";", dto.getToList().toArray(new String[]{})));
            record.setCreate_time(new Date());
            record.setCreate_user(BaseUtils.GetUserFromContext());
            record.setStatus("sending");
            SysMailRecord sendRec = mailRecRepository.save(record);
            CompletableFuture.supplyAsync(() -> send(mailConfig, dto))
                    .exceptionally(this::getDepthCause)
                    .thenApplyAsync((result) -> {
                        if (result.equals("success")) {
                            sendRec.setStatus("success");
                        } else {
                            sendRec.setStatus("failed");
                            sendRec.setReason(result);
                        }
                        return mailRecRepository.save(sendRec);
                    });
            return "success";
        }
        return "config not found";
    }

    public String send(SysMailConfig mailConfig, MailSendDto dto) {
        try {
            Session session = initProperties(mailConfig);
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setDefaultEncoding(mailConfig.getEncode());
            mailSender.setSession(session);
            MimeMessage mailMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true, mailConfig.getEncode());
            helper.setValidateAddresses(true);
            helper.setFrom(mailConfig.getEmail(), "RedisMonitorService");
            helper.setTo(dto.getToList().toArray(new String[]{}));
            if (dto.getCcList().size() > 0) {
                helper.setCc(dto.getCcList().toArray(new String[]{}));
            }
            helper.setSubject(dto.getSubject());
            helper.setText("", dto.getContent());
            mailSender.send(mailMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            return getDepthCause(e);
        }
        return "success";
    }

    private String getDepthCause(Throwable ex) {
        String emsg = "";
        Throwable t = ex.getCause();
        while (t != null) {
            emsg = t.getMessage();
            t = t.getCause();
        }
        return emsg;
    }

    private Session initProperties(SysMailConfig conf) {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", conf.getProtocol());
        properties.setProperty("mail.smtp.host", conf.getHost());
        properties.setProperty("mail.smtp.port", String.valueOf(conf.getPort()));
        // 使用smtp身份验证
        MailSSLSocketFactory mailSSLSocketFactory = null;
        try {
            mailSSLSocketFactory = new MailSSLSocketFactory();
            mailSSLSocketFactory.setTrustAllHosts(true);
            mailSSLSocketFactory.setKeyManagers(kmf.getKeyManagers());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.enable", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
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
        session.setDebug(false);
        return session;
    }
}
