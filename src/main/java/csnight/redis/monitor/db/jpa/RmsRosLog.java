package csnight.redis.monitor.db.jpa;

import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rms_log_ros")
public class RmsRosLog implements RmsLog {
    @Id
    @Column(name = "id", length = 50)
    @JSONField(ordinal = 1)
    private String id;
    @Column(name = "ins_id", length = 50)
    @JSONField(ordinal = 2)
    private String ins_id;
    @Column(name = "tm")
    @Temporal(TemporalType.TIMESTAMP)
    @JSONField(ordinal = 3)
    private Date tm;
    @Column(name = "sector")
    @JSONField(ordinal = 4)
    private String sector;
    @Column(name = "tcs")
    @JSONField(ordinal = 5)
    private long tcs;
    @Column(name = "ops")
    @JSONField(ordinal = 6)
    private long ops;
    @Column(name = "pub_ch")
    @JSONField(ordinal = 7)
    private int pub_ch;
    @Column(name = "pub_pat")
    @JSONField(ordinal = 8)
    private int pub_pat;
    @Column(name = "cmd_stat")
    @JSONField(ordinal = 9)
    private String cmd_stat;

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

    public Date getTm() {
        return tm;
    }

    public void setTm(Date tm) {
        this.tm = tm;
    }

    @Override
    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public long getTcs() {
        return tcs;
    }

    public void setTcs(long tcs) {
        this.tcs = tcs;
    }

    public long getOps() {
        return ops;
    }

    public void setOps(long ops) {
        this.ops = ops;
    }

    public int getPub_ch() {
        return pub_ch;
    }

    public void setPub_ch(int pub_ch) {
        this.pub_ch = pub_ch;
    }

    public int getPub_pat() {
        return pub_pat;
    }

    public void setPub_pat(int pub_pat) {
        this.pub_pat = pub_pat;
    }

    public String getCmd_stat() {
        return cmd_stat;
    }

    public void setCmd_stat(String cmd_stat) {
        this.cmd_stat = cmd_stat;
    }
}
