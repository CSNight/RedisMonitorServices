package csnight.redis.monitor.db.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @author csnight
 * @description
 * @since 2020-1-18 21:33
 */

@Entity
@Table(name = "rms_shake_record")
public class RmsShakeRecord {
    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 50)
    private String id;
    @Column(name = "source_ins")
    private String source_ins;
    @Column(name = "target_ins")
    private String target_ins;
    @Column(name = "shake_type")
    private String shake_type;
    @Column(name = "relate_backup")
    private String relate_backup;
    @Column(name = "conf")
    private String conf;
    @Column(name = "result")
    private String result;
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;
    @Column(name = "create_user")
    private String create_user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource_ins() {
        return source_ins;
    }

    public void setSource_ins(String source_ins) {
        this.source_ins = source_ins;
    }

    public String getTarget_ins() {
        return target_ins;
    }

    public void setTarget_ins(String target_ins) {
        this.target_ins = target_ins;
    }

    public String getShake_type() {
        return shake_type;
    }

    public void setShake_type(String shake_type) {
        this.shake_type = shake_type;
    }

    public String getRelate_backup() {
        return relate_backup;
    }

    public void setRelate_backup(String relate_backup) {
        this.relate_backup = relate_backup;
    }

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }
}
