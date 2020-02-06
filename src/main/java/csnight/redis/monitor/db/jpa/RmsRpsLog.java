package csnight.redis.monitor.db.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rms_log_rps")
public class RmsRpsLog {
    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 50)
    private String id;
    @Column(name = "ins_id", length = 50)
    private String ins_id;
    @Column(name = "tm")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tm;
    @Column(name = "mem_us")
    private long mem_us;
    @Column(name = "mem_rs")
    private long mem_rs;
    @Column(name = "mem_ds")
    private long mem_ds;
    @Column(name = "mem_oh")
    private long mem_oh;
    @Column(name = "mem_fr")
    private double mem_fr;
    @Column(name = "mem_frb")
    private long mem_frb;
    @Column(name = "mem_peak")
    private long mem_peak;
    @Column(name = "cpu_uu")
    private double cpu_uu;
    @Column(name = "cpu_su")
    private double cpu_su;
    @Column(name = "cpu_per")
    private double cpu_per;
    @Column(name = "ioo")
    private long ioo;
    @Column(name = "ioi")
    private long ioi;
    @Column(name = "io_iik")
    private double io_iik;
    @Column(name = "io_iok")
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
