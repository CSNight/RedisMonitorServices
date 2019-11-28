package csnight.redis.monitor.db.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_operation_log")
public class SysOpLog {
    public SysOpLog() {
    }

    public SysOpLog(String un, String op, String ip, String mo, int st, String auth, Long cost) {
        this.ct = new Date();
        this.un = un;
        this.op = op;
        this.ip = ip;
        this.mo = mo;
        this.st = st;
        this.auth = auth;
        this.cost = cost;
        if (this.st < 300) {
            this.level = "INFO";
        } else if (this.st > 300 && this.st < 400) {
            this.level = "WARN";
        } else {
            this.level = "ERROR";
        }
    }

    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 50)
    private String id;
    @Column(name = "un")
    private String un;
    @Column(name = "ct")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ct;
    @Column(name = "ip")
    private String ip;
    @Column(name = "op")
    private String op;
    @Column(name = "mo")
    private String mo;
    @Column(name = "auth")
    private String auth;
    @Column(name = "level")
    private String level;
    @Column(name = "st")
    private int st;
    @Column(name = "cost")
    private Long cost;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUn() {
        return un;
    }

    public void setUn(String un) {
        this.un = un;
    }

    public Date getCt() {
        return ct;
    }

    public void setCt(Date ct) {
        this.ct = ct;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getMo() {
        return mo;
    }

    public void setMo(String mo) {
        this.mo = mo;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }
}
