package com.csnight.redis.monitor.db.jpa;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sys_role")
public class SysRole {

    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id")
    private String id;
    @Column(name = "code ")
    private String code;
    @Column(name = "name ")
    private String name;
    @Column(name = "level ")
    private int level;
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;
    //急加载 会查询role表
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "sys_role_permission",
            inverseJoinColumns = {@JoinColumn(name = "permission_id", referencedColumnName = "id")},
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    private Set<SysPermission> Permission = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "sys_role_menu",
            inverseJoinColumns = {@JoinColumn(name = "menu_id", referencedColumnName = "id")},
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    private Set<SysMenu> menus = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Set<SysPermission> getPermission() {
        return Permission;
    }

    public void setPermission(Set<SysPermission> permission) {
        Permission = permission;
    }

    public Set<SysMenu> getMenus() {
        return menus;
    }

    public void setMenus(Set<SysMenu> menus) {
        this.menus = menus;
    }
}