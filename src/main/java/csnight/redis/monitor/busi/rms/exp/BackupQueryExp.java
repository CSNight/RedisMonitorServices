package csnight.redis.monitor.busi.rms.exp;

import csnight.redis.monitor.db.blurry.Query;

import java.util.Date;

public class BackupQueryExp {
    @Query(type = Query.Type.INNER_LIKE)
    private String filename;
    @Query(type = Query.Type.EQUAL)
    private String backup_type;
    @Query(type = Query.Type.GREATER_THAN, propName = "create_time")
    private Date std;
    @Query(type = Query.Type.LESS_THAN, propName = "create_time")
    private Date etd;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getBackup_type() {
        return backup_type;
    }

    public void setBackup_type(String backup_type) {
        this.backup_type = backup_type;
    }

    public Date getStd() {
        return std;
    }

    public void setStd(Date std) {
        this.std = std;
    }

    public Date getEtd() {
        return etd;
    }

    public void setEtd(Date etd) {
        this.etd = etd;
    }
}
