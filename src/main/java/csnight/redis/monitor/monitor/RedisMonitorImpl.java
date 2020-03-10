package csnight.redis.monitor.monitor;

import com.sun.mail.util.MailSSLSocketFactory;
import csnight.redis.monitor.db.jpa.SysMailConfig;
import csnight.redis.monitor.db.jpa.SysMailRecord;
import csnight.redis.monitor.db.repos.*;
import csnight.redis.monitor.rest.sys.dto.MailSendDto;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.utils.ReflectUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.KeyManagerFactory;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RedisMonitorImpl implements RedisMonitor {
    private MonitorState state = MonitorState.INITIALIZE;
    private Map<Long, Double> samples = new LinkedHashMap<>();
    private long start = 0L;
    private long end = 0L;
    private String ins_id;
    private int range;
    private String indicator;
    private String signal;
    private String logic;
    private double valf;
    private double vals;
    private String clazz;
    private String contact;
    private String subject;
    private String uid;
    private int delay = -1;

    public RedisMonitorImpl(String rule, String uid, String ins_id) {
        Initialize(rule, uid, ins_id);
    }

    private void Initialize(String rule, String uid, String ins_id) {
        try {
            this.uid = uid;
            this.ins_id = ins_id;
            String[] parts = rule.split("\\|");
            this.range = Integer.parseInt(parts[2]) * Integer.parseInt(parts[3]);
            this.indicator = parts[1];
            this.signal = parts[4];
            String unit = parts[parts.length - 4];
            this.clazz = parts[parts.length - 3];
            this.contact = parts[parts.length - 2];
            this.subject = parts[parts.length - 1];
            if (parts.length == 11) {
                this.logic = parts[5];
                valf = unit.equals("percent") ? Double.parseDouble(parts[6]) / 100.0 : Double.parseDouble(parts[6]);
            } else {
                this.logic = parts[6];
                valf = unit.equals("percent") ? Double.parseDouble(parts[5]) / 100.0 : Double.parseDouble(parts[5]);
                vals = unit.equals("percent") ? Double.parseDouble(parts[7]) / 100.0 : Double.parseDouble(parts[7]);
            }
            state = MonitorState.MONITORING;
        } catch (Exception ex) {
            state = MonitorState.COMPLETE;
        }
    }

    @Override
    public MonitorState getState() {
        return state;
    }

    @Override
    public void setState(MonitorState state) {
        this.state = state;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public void setSamples(double val) {
        if (this.start == 0L) {
            this.start = System.currentTimeMillis();
            this.end = this.start;
        } else {
            this.end = System.currentTimeMillis();
        }
        this.samples.put(this.end, val);
        if ((this.end - this.start) / 1000 >= this.range) {
            needNotify();
        }
    }

    private void needNotify() {
        double val;
        Collection<Double> sav = samples.values();
        switch (this.signal) {
            default:
            case "mean":
                double total = 0;
                for (Double v : sav) {
                    total += v;
                }
                val = total / sav.size();
                break;
            case "max":
                double max = Double.NEGATIVE_INFINITY;
                for (Double v : sav) {
                    if (v >= max) {
                        max = v;
                    }
                }
                val = max;
                break;
            case "min":
                double min = Double.POSITIVE_INFINITY;
                for (Double v : sav) {
                    if (v <= min) {
                        min = v;
                    }
                }
                val = min;
                break;
        }
        boolean notify;
        if (this.logic.equals("between")) {
            notify = val >= this.valf && val <= this.vals;
        } else {
            switch (this.logic) {
                default:
                case "=":
                    notify = val == this.valf;
                    break;
                case "<":
                    notify = val < this.valf;
                    break;
                case "<=":
                    notify = val <= this.valf;
                    break;
                case ">":
                    notify = val > this.valf;
                    break;
                case ">=":
                    notify = val >= this.valf;
                    break;
                case "!=":
                    notify = val != this.valf;
                    break;
            }
        }
        if (notify) {
            String info = getMailTemplate(val);
            SendMail(info);
        } else {
            state = MonitorState.COMPLETE;
        }
    }

    private void SendMail(String info) {
        state = MonitorState.MAILING;
        MailSendDto dto = new MailSendDto();
        dto.setUid(this.uid);
        dto.setContent(info);
        dto.setSubject(subject);
        dto.getToList().add(contact);
        SysMailConfRepository mailConfRepository = ReflectUtils.getBean(SysMailConfRepository.class);
        SysMailRecRepository mailRecRepository = ReflectUtils.getBean(SysMailRecRepository.class);
        SysMailConfig mailConfig = mailConfRepository.findByUid(dto.getUid());
        if (mailConfig != null) {
            SysMailRecord record = new SysMailRecord();
            record.setEmail(mailConfig.getEmail());
            record.setFlag(IdentifyUtils.string2MD5(dto.getContent(), ""));
            record.setTheme(dto.getSubject());
            record.setMt("CUSTOM");
            record.setTos(String.join(";", dto.getToList().toArray(new String[]{})));
            record.setCt(new Date());
            record.setCreate_user(ReflectUtils.getBean(SysUserRepository.class).findUsernameById(uid));
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
                        state = MonitorState.COMPLETE;
                        return mailRecRepository.save(sendRec);
                    });
            return;
        }
        state = MonitorState.COMPLETE;
    }

    private String send(SysMailConfig mailConfig, MailSendDto dto) {
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
        } catch (Exception e) {
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
            mailSSLSocketFactory.setKeyManagers(ReflectUtils.getBean(KeyManagerFactory.class).getKeyManagers());
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
        session.setDebug(true);
        return session;
    }

    private String getMailTemplate(double val) {
        String level;
        String insName = ReflectUtils.getBean(RmsInsRepository.class).findOnly(ins_id).getInstance_name();
        String indicatorLabel = ReflectUtils.getBean(RmsIndicatorRepository.class).findByName(this.indicator).getLabel();
        switch (this.clazz) {
            default:
            case "info":
                level = "通知";
                break;
            case "warning":
                level = "警告";
                break;
            case "danger":
                level = "危险告警";
                break;
        }
        String sign;
        switch (this.signal) {
            default:
            case "mean":
                sign = "平均值";
                break;
            case "max":
                sign = "最大值";
                break;
            case "min":
                sign = "最小值";
                break;
        }
        String expressionF = "%s %.2f";
        String express;
        if (this.logic.equals("between")) {
            expressionF += " ~ %.2f";
            express = String.format(expressionF, this.logic, this.valf, this.vals);
        } else {
            express = String.format(expressionF, this.logic, this.valf);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone((TimeZone.getDefault()));
        return String.format("Redis Monitor Service %s: 在%s 至 %s时间段内监测到Redis数据库%s %s指标的%s超出监控规则范围！具体情况如下:\r\n%s为%.2f, 指标范围为:%s", level,
                sdf.format(new Date(this.start)),
                sdf.format(new Date(this.end)), insName, indicatorLabel, sign, sign, val, express);
    }

    @Override
    public void destroy() {
        samples.clear();
        samples = null;
    }
}
