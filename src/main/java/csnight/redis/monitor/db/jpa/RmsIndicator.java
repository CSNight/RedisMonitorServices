package csnight.redis.monitor.db.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rms_indicators")
public class RmsIndicator {
    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 50)
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "label")
    private String label;
    @Column(name = "sign_support")
    private String sign_support;
    @Column(name = "exp_support")
    private String exp_support;
    @Column(name = "unit")
    private String unit;
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSign_support() {
        return sign_support;
    }

    public void setSign_support(String sign_support) {
        this.sign_support = sign_support;
    }

    public String getExp_support() {
        return exp_support;
    }

    public void setExp_support(String exp_support) {
        this.exp_support = exp_support;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}
