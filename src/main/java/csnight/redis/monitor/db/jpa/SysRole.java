package csnight.redis.monitor.db.jpa;


import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;
    @Column(name = "level")
    private int level;
    @Column(name = "ct")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ct;
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

    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    @JsonIgnore
    @JSONField(serialize = false)
    private Set<SysUser> users = new HashSet<>();

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

    public Date getCt() {
        return ct;
    }

    public void setCt(Date ct) {
        this.ct = ct;
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

    public Set<SysUser> getUsers() {
        return users;
    }

    public void setUsers(Set<SysUser> users) {
        this.users = users;
    }
}