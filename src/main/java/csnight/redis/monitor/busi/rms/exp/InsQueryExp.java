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
    @Query(type = Query.Type.INNER_LIKE)
    private String user_id;

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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
