package csnight.redis.monitor.busi.rms.exp;

import csnight.redis.monitor.db.blurry.Query;

import java.util.HashSet;
import java.util.Set;

public class InsQueryExp {
    @Query(type = Query.Type.IN, propName = "id")
    private Set<String> ids = new HashSet<>();
    @Query(blurry = "instance_name,ip")
    private String blurry;
    @Query
    private Boolean state;

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    public String getBlurry() {
        return blurry;
    }

    public void setBlurry(String blurry) {
        this.blurry = blurry;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }
}
