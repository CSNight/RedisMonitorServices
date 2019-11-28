package com.redis.monitor.busi.sys.exp;

public class MenuMeta {
    private String title;
    private String ref;
    private String icon;

    MenuMeta(String title, String ref, String icon) {
        this.title = title;
        this.ref = ref;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
