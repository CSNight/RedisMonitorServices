package csnight.redis.monitor.db.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @author csnight
 * @description
 * @since 2020/1/20 14:10
 */
@Entity
@Table(name = "rms_data_record")
public class RmsDataRecord {
    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 50)
    private String id;
    @Column(name = "ins_id", length = 50)
    private String ins_id;
    @Column(name = "backup_type", length = 50)
    private String backup_type;
    @Column(name = "filename", length = 200)
    private String filename;
    @Column(name = "size")
    private long size;
    @Column(name = "dl_count")
    private int dl_count;
    @Column(name = "last_down")
    @Temporal(TemporalType.TIMESTAMP)
    private Date last_down;
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;
    @Column(name = "create_user")
    private String create_user;

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

    public String getBackup_type() {
        return backup_type;
    }

    public void setBackup_type(String backup_type) {
        this.backup_type = backup_type;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getDl_count() {
        return dl_count;
    }

    public void setDl_count(int dl_count) {
        this.dl_count = dl_count;
    }

    public Date getLast_down() {
        return last_down;
    }

    public void setLast_down(Date last_down) {
        this.last_down = last_down;
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
}
