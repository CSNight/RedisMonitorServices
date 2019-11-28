package com.redis.monitor.db.jpa;

import javax.persistence.*;

@Entity
@Table(name = "sys_icons")
public class SysIcons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "_class")
    private String _class;
    @Column(name = "_group")
    private String _group;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public String get_group() {
        return _group;
    }

    public void set_group(String _group) {
        this._group = _group;
    }
}
