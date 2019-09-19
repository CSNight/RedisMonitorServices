package com.csnight.redis.monitor.auth.jpa;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "sys_role")
public class SysRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(length = 25)
    private String name;
    //懒加载 不会查询role表
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private List<SysUser> users;
    //急加载 会查询role表
    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    private List<SysPermission> Permission;

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

    public List<SysUser> getUsers() {
        return users;
    }

    public void setUsers(List<SysUser> users) {
        this.users = users;
    }

    public List<SysPermission> getPermission() {
        return Permission;
    }

    public void setPermission(List<SysPermission> permission) {
        Permission = permission;
    }
}