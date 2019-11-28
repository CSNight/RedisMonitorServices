package com.redis.monitor.busi.sys.exp;

import com.redis.monitor.db.blurry.Query;

public class RoleQueryExp {
    @Query(blurry = "name,code")
    private String blurry;

    public String getBlurry() {
        return blurry;
    }

    public void setBlurry(String blurry) {
        this.blurry = blurry;
    }
}
