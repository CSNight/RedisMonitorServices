package com.csnight.redis.monitor.busi.sys.exp;

import com.csnight.redis.monitor.db.blurry.Query;

public class UserQueryExp {
    @Query(blurry = "username,phone")
    private String blurry;

    public String getBlurry() {
        return blurry;
    }

    public void setBlurry(String blurry) {
        this.blurry = blurry;
    }
}
