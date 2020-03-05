package csnight.redis.monitor.monitor;

import csnight.redis.monitor.busi.sys.MailSendServiceImpl;
import csnight.redis.monitor.rest.sys.dto.MailSendDto;
import csnight.redis.monitor.utils.ReflectUtils;

import java.util.HashMap;
import java.util.Map;

public class RedisMonitorImpl {
    private Map<Long, Double> samples = new HashMap<>();
    private int cycle;
    private int duration;
    private String ins_id;
    private String indicator;
    private String expression;
    private String clazz;
    private String contact;
    private String subject;
    private String uid;

    public RedisMonitorImpl(String rule) {

    }

    public void SendMail(String info) {
        MailSendServiceImpl mailSendService = ReflectUtils.getBean(MailSendServiceImpl.class);
        MailSendDto dto = new MailSendDto();
        dto.setUid(uid);
        dto.setContent(getMailTemplate(clazz, info));
        dto.setSubject(subject);
        dto.getToList().add(contact);
        mailSendService.SendMail(dto);
    }

    private String getMailTemplate(String clazz, String info) {
        return "";
    }
}
