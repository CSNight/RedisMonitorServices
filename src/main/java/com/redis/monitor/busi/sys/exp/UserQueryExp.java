package com.redis.monitor.busi.sys.exp;

import com.redis.monitor.db.blurry.Query;

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
