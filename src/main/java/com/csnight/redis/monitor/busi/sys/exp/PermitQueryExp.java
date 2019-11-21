package com.csnight.redis.monitor.busi.sys.exp;

import com.csnight.redis.monitor.db.blurry.Query;

public class PermitQueryExp {
    @Query(blurry = "name,description")
    private String blurry;

    public String getBlurry() {
        return blurry;
    }

    public void setBlurry(String blurry) {
        this.blurry = blurry;
    }
}
