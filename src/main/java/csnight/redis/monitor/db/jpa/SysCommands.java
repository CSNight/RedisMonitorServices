package csnight.redis.monitor.db.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_commands")
public class SysCommands {
    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 50)
    private String id;
    @Column(name = "commands")
    private String commands;
    @Column(name = "create_user")
    private String create_user;
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}
