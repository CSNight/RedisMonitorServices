package csnight.redis.monitor.db.jpa;

import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rms_log_rps")
public class RmsRpsLog implements RmsLog {
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
    @Column(name = "mem_us")
    @JSONField(ordinal = 5)
    private long mem_us;
    @Column(name = "mem_rs")
    @JSONField(ordinal = 6)
    private long mem_rs;
    @Column(name = "mem_ds")
    @JSONField(ordinal = 7)
    private long mem_ds;
    @Column(name = "mem_oh")
    @JSONField(ordinal = 8)
    private long mem_oh;
    @Column(name = "mem_fr")
    @JSONField(ordinal = 9)
    private double mem_fr;
    @Column(name = "mem_frb")
    @JSONField(ordinal = 10)
    private long mem_frb;
    @Column(name = "mem_peak")
    @JSONField(ordinal = 11)
    private long mem_peak;
    @Column(name = "cpu_uu")
    @JSONField(ordinal = 12)
    private double cpu_uu;
    @Column(name = "cpu_su")
    @JSONField(ordinal = 13)
    private double cpu_su;
    @Column(name = "cpu_per")
    @JSONField(ordinal = 14)
    private double cpu_per;
    @Column(name = "ioo")
    @JSONField(ordinal = 15)
    private long ioo;
    @Column(name = "ioi")
    @JSONField(ordinal = 16)
    private long ioi;
    @Column(name = "io_iik")
    @JSONField(ordinal = 17)
    private double io_iik;
    @Column(name = "io_iok")
    @JSONField(ordinal = 18)
    private double io_iok;

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

    public long getMem_us() {
        return mem_us;
    }

    public void setMem_us(long mem_us) {
        this.mem_us = mem_us;
    }

    public long getMem_rs() {
        return mem_rs;
    }

    public void setMem_rs(long mem_rs) {
        this.mem_rs = mem_rs;
    }

    public long getMem_ds() {
        return mem_ds;
    }

    public void setMem_ds(long mem_ds) {
        this.mem_ds = mem_ds;
    }

    public long getMem_oh() {
        return mem_oh;
    }

    public void setMem_oh(long mem_oh) {
        this.mem_oh = mem_oh;
    }

    public double getMem_fr() {
        return mem_fr;
    }

    public void setMem_fr(double mem_fr) {
        this.mem_fr = mem_fr;
    }

    public long getMem_frb() {
        return mem_frb;
    }

    public void setMem_frb(long mem_frb) {
        this.mem_frb = mem_frb;
    }

    public long getMem_peak() {
        return mem_peak;
    }

    public void setMem_peak(long mem_peak) {
        this.mem_peak = mem_peak;
    }

    public double getCpu_uu() {
        return cpu_uu;
    }

    public void setCpu_uu(double cpu_uu) {
        this.cpu_uu = cpu_uu;
    }

    public double getCpu_su() {
        return cpu_su;
    }

    public void setCpu_su(double cpu_su) {
        this.cpu_su = cpu_su;
    }

    public double getCpu_per() {
        return cpu_per;
    }

    public void setCpu_per(double cpu_per) {
        this.cpu_per = cpu_per;
    }

    public long getIoo() {
        return ioo;
    }

    public void setIoo(long ioo) {
        this.ioo = ioo;
    }

    public long getIoi() {
        return ioi;
    }

    public void setIoi(long ioi) {
        this.ioi = ioi;
    }

    public double getIo_iik() {
        return io_iik;
    }

    public void setIo_iik(double io_iik) {
        this.io_iik = io_iik;
    }

    public double getIo_iok() {
        return io_iok;
    }

    public void setIo_iok(double io_iok) {
        this.io_iok = io_iok;
    }
}
