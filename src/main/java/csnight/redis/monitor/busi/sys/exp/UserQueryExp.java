package csnight.redis.monitor.busi.sys.exp;

import csnight.redis.monitor.db.blurry.Query;

import java.util.HashSet;
import java.util.Set;

public class UserQueryExp {
    @Query(type = Query.Type.IN, propName = "id")
    private Set<String> ids = new HashSet<>();
    @Query(blurry = "username,phone")
    private String blurry;

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
}
