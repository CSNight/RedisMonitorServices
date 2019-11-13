package com.csnight.redis.monitor.db.jpa;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "sys_role")
public class SysRole {

    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id")
    private String id;

    @Column(name = "name ", length = 25)
    private String name;

    //急加载 会查询role表
    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    private Set<SysPermission> Permission;

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


    public Set<SysPermission> getPermission() {
        return Permission;
    }

    public void setPermission(Set<SysPermission> permission) {
        Permission = permission;
    }
}