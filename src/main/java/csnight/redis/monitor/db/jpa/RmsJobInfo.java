package csnight.redis.monitor.db.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rms_jobs_info")
public class RmsJobInfo {
    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 50)
    private String id;
    @Column(name = "job_name")
    private String job_name;
    @Column(name = "job_group")
    private String job_group;
    @Column(name = "trigger_type")
    private int trigger_type;
    @Column(name = "job_class")
    private String job_class;
    @Column(name = "job_describe")
    private String job_describe;
    @Column(name = "job_config")
    private String job_config;
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;
    @Column(name = "create_user")
    private String create_user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ins_id")
    private RmsInstance instance;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public String getJob_group() {
        return job_group;
    }

    public void setJob_group(String job_group) {
        this.job_group = job_group;
    }

    public int getTrigger_type() {
        return trigger_type;
    }

    public void setTrigger_type(int trigger_type) {
        this.trigger_type = trigger_type;
    }

    public String getJob_class() {
        return job_class;
    }

    public void setJob_class(String job_class) {
        this.job_class = job_class;
    }

    public String getJob_describe() {
        return job_describe;
    }

    public void setJob_describe(String job_describe) {
        this.job_describe = job_describe;
    }

    public String getJob_config() {
        return job_config;
    }

    public void setJob_config(String job_config) {
        this.job_config = job_config;
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

    public RmsInstance getInstance() {
        return instance;
    }

    public void setInstance(RmsInstance instance) {
        this.instance = instance;
    }
}
