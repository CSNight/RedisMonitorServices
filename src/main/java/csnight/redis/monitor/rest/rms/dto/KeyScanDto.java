package csnight.redis.monitor.rest.rms.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class KeyScanDto {
    @NotEmpty
    @NotNull
    private String ins_id;
    @Min(0)
    private int db;
    @NotNull
    private String cursor;
    @Max(1000)
    @Min(10)
    private int count = 10;
    @NotEmpty
    @NotNull
    private String match = "*";

    public String getIns_id() {
        return ins_id;
    }

    public void setIns_id(String ins_id) {
        this.ins_id = ins_id;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }
}
