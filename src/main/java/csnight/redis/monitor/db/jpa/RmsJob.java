package csnight.redis.monitor.db.jpa;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rms_jobs")
public class RmsJob {
    @Id
    @Column(name = "id", length = 50)
    private String id;
    @Column(name = "ins_id", length = 50)
    private String ins_id;
    @Column(name = "job_name")
    private String job_name;
    @Column(name = "job_group")
    private String job_group;
    @Column(name = "trigger_type")
    private int trigger_type;
    @Column(name = "job_func")
    private String job_func;
    @Column(name = "job_class")
    private String job_class;
    @Column(name = "job_describe")
    private String job_describe;
    @Column(name = "job_config")
    private String job_config;
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;
    @ManyToMany(mappedBy = "jobs", fetch = FetchType.EAGER)
    @JsonIgnore
    @JSONField(serialize = false)
    private Set<RmsInstance> instances = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIns_id() {
        return ins_id;
    }

    public void setIns_id(String ins_id) {
        this.ins_id = ins_id;
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

    public String getJob_func() {
        return job_func;
    }

    public void setJob_func(String job_func) {
        this.job_func = job_func;
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

    public Set<RmsInstance> getInstances() {
        return instances;
    }

    public void setInstances(Set<RmsInstance> instances) {
        this.instances = instances;
    }
}
