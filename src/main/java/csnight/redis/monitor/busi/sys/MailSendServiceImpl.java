package csnight.redis.monitor.busi.sys;

import com.sun.mail.util.MailSSLSocketFactory;
import csnight.redis.monitor.busi.sys.exp.MailRecQueryExp;
import csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import csnight.redis.monitor.db.jpa.SysMailConfig;
import csnight.redis.monitor.db.jpa.SysMailRecord;
import csnight.redis.monitor.db.jpa.SysRole;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysMailConfRepository;
import csnight.redis.monitor.db.repos.SysMailRecRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.rest.sys.dto.MailConfDto;
import csnight.redis.monitor.rest.sys.dto.MailDelDto;
import csnight.redis.monitor.rest.sys.dto.MailSendDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.springframework.data.domain.Sort;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service

public class MailSendServiceImpl {
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

    public List<SysMailRecord> GetAllMailRecords() {
        return mailRecRepository.findAll(Sort.by(Sort.Direction.DESC, "ct"));
    }

    public SysMailConfig GetUserMailConfig() {
        String uid = userRepository.findIdByUsername(BaseUtils.GetUserFromContext());
        return mailConfRepository.findByUid(uid);
    }

    public List<SysMailRecord> GetUserMailRecord() {
        return mailRecRepository.findAllByUser(BaseUtils.GetUserFromContext());
    }

    public SysMailRecord GetUserMailRecordById(String id) {
        Optional<SysMailRecord> record = mailRecRepository.findById(id);
        return record.orElse(null);
    }

    public String DeleteRecordById(String id) {
        if (mailRecRepository.existsById(id)) {
            mailRecRepository.deleteById(id);
            return "success";
        }
        return "failed";
    }

    public List<SysMailRecord> QueryMailRecord(MailRecQueryExp exp) {
        List<SysMailRecord> records = mailRecRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        String username = BaseUtils.GetUserFromContext();
        SysUser user = userRepository.findByUsername(username);
        if (user != null) {
            Set<String> roles = user.getRoles().stream().map(SysRole::getCode).collect(Collectors.toSet());
            if (roles.contains("ROLE_DEV") || roles.contains("ROLE_SUPER")) {
                return records;
            } else {
                return records.stream().filter(rec -> rec.getCreate_user().equals(username)).collect(Collectors.toList());
            }
        } else {
            return new ArrayList<>();
        }
    }

    public String DeleteRecordMulti(MailDelDto dto) {
        MailRecQueryExp exp = new MailRecQueryExp();
        exp.setIds(dto.getIds());
        List<SysMailRecord> records = mailRecRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        if (records.size() > 0) {
            mailRecRepository.deleteInBatch(records);
            return "success";
        }
        return "failed";
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

    public void DeleteUserMailResource(String username) {
        String uid = userRepository.findIdByUsername(username);
        SysMailConfig mailConfig = mailConfRepository.findByUid(uid);
        if (mailConfig != null) {
            mailConfRepository.deleteById(mailConfig.getId());
        }
        List<SysMailRecord> records = mailRecRepository.findAllByUser(username);
        mailRecRepository.deleteInBatch(records);
    }

    public String SendMail(MailSendDto dto) {
        SysMailConfig mailConfig = mailConfRepository.findByUid(dto.getUid());
        if (mailConfig != null) {
            SysMailRecord record = new SysMailRecord();
            record.setEmail(mailConfig.getEmail());
            record.setFlag(IdentifyUtils.string2MD5(dto.getContent(), ""));
            record.setTheme(dto.getSubject());
            record.setMt("CUSTOM");
            record.setTos(String.join(";", dto.getToList().toArray(new String[]{})));
            record.setCt(new Date());
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
            return sendRec.getId();
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
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(conf.getEmail(), conf.getPwd());
            }
        });
        session.setDebug(false);
        return session;
    }
}
