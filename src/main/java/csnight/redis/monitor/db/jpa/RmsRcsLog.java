package csnight.redis.monitor.db.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rms_log_rcs")
public class RmsRcsLog implements RmsLog {
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
    @Column(name = "sector")
    private String sector;
    @Column(name = "cli_con")
    private int cli_con;
    @Column(name = "cli_blo")
    private int cli_blo;
    @Column(name = "reject_cons")
    private long reject_cons;
    @Column(name = "total_cons_rec")
    private long total_cons_rec;

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

    public int getCli_con() {
        return cli_con;
    }

    public void setCli_con(int cli_con) {
        this.cli_con = cli_con;
    }

    public int getCli_blo() {
        return cli_blo;
    }

    public void setCli_blo(int cli_blo) {
        this.cli_blo = cli_blo;
    }

    public long getReject_cons() {
        return reject_cons;
    }

    public void setReject_cons(long reject_cons) {
        this.reject_cons = reject_cons;
    }

    public long getTotal_cons_rec() {
        return total_cons_rec;
    }

    public void setTotal_cons_rec(long total_cons_rec) {
        this.total_cons_rec = total_cons_rec;
    }
}
