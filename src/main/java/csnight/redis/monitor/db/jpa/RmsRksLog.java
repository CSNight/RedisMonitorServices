package csnight.redis.monitor.db.jpa;

import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rms_log_rks")
public class RmsRksLog implements RmsLog {
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
    @Column(name = "key_size")
    @JSONField(ordinal = 5)
    private long key_size;
    @Column(name = "exp_keys")
    @JSONField(ordinal = 6)
    private long exp_keys;
    @Column(name = "exp_kps")
    @JSONField(ordinal = 7)
    private double exp_kps;
    @Column(name = "evc_keys")
    @JSONField(ordinal = 8)
    private long evc_keys;
    @Column(name = "evc_kps")
    @JSONField(ordinal = 9)
    private double evc_kps;
    @Column(name = "ksp_hits")
    @JSONField(ordinal = 10)
    private long ksp_hits;
    @Column(name = "ksp_hits_ps")
    @JSONField(ordinal = 11)
    private double ksp_hits_ps;
    @Column(name = "ksp_miss")
    @JSONField(ordinal = 12)
    private long ksp_miss;
    @Column(name = "ksp_miss_ps")
    @JSONField(ordinal = 13)
    private double ksp_miss_ps;
    @Column(name = "db_info")
    @JSONField(ordinal = 14)
    private String db_info;

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

    public long getKey_size() {
        return key_size;
    }

    public void setKey_size(long key_size) {
        this.key_size = key_size;
    }

    public long getExp_keys() {
        return exp_keys;
    }

    public void setExp_keys(long exp_keys) {
        this.exp_keys = exp_keys;
    }

    public long getEvc_keys() {
        return evc_keys;
    }

    public void setEvc_keys(long evc_keys) {
        this.evc_keys = evc_keys;
    }

    public double getExp_kps() {
        return exp_kps;
    }

    public void setExp_kps(double exp_kps) {
        this.exp_kps = exp_kps;
    }

    public double getEvc_kps() {
        return evc_kps;
    }

    public void setEvc_kps(double evc_kps) {
        this.evc_kps = evc_kps;
    }

    public long getKsp_hits() {
        return ksp_hits;
    }

    public void setKsp_hits(long ksp_hits) {
        this.ksp_hits = ksp_hits;
    }

    public double getKsp_hits_ps() {
        return ksp_hits_ps;
    }

    public void setKsp_hits_ps(double ksp_hits_ps) {
        this.ksp_hits_ps = ksp_hits_ps;
    }

    public long getKsp_miss() {
        return ksp_miss;
    }

    public void setKsp_miss(long ksp_miss) {
        this.ksp_miss = ksp_miss;
    }

    public double getKsp_miss_ps() {
        return ksp_miss_ps;
    }

    public void setKsp_miss_ps(double ksp_miss_ps) {
        this.ksp_miss_ps = ksp_miss_ps;
    }

    public String getDb_info() {
        return db_info;
    }

    public void setDb_info(String db_info) {
        this.db_info = db_info;
    }
}
