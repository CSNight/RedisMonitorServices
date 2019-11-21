package com.csnight.redis.monitor.busi.sys.exp;

import com.csnight.redis.monitor.db.blurry.Query;

import java.util.HashSet;
import java.util.Set;

public class MenuQueryExp {
    @Query(type = Query.Type.IN, propName = "id")
    private Set<Long> ids = new HashSet<>();

    @Query(type = Query.Type.INNER_LIKE)
    private String name;
    @Query
    private Boolean hidden;

    @Query
    private Long pid;

    public Set<Long> getIds() {
        return ids;
    }

    public void setIds(Set<Long> ids) {
        this.ids = ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }
}
