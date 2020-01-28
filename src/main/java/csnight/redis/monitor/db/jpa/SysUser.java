package csnight.redis.monitor.db.jpa;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "sys_user")
public class SysUser implements UserDetails {
    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "username", length = 200, unique = true)
    private String username;

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "nick_name", length = 100)
    private String nick_name;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 11)
    private String phone;

    @Column(name = "last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private Date last_login;

    @Column(name = "login_times")
    private int login_times;

    @Column(name = "lock_by")
    private String lock_by;
    @Column(name = "org_id")
    private Long org_id;
    @Lob
    @Column(name = "head_img")
    private byte[] head_img;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "sys_role_user",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}
    )
    private Set<SysRole> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Set<SysRole> roles = this.getRoles();
        Set<String> permissions = new HashSet<>();
        for (SysRole role : roles) {
            role.getPermission().forEach(permit -> permissions.add(permit.getName()));
        }
        for (String permission : permissions)
            authorities.add(new SimpleGrantedAuthority(permission));
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.getEnabled();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getLast_login() {
        return last_login;
    }

    public void setLast_login(Date last_login) {
        this.last_login = last_login;
    }

    public int getLogin_times() {
        return login_times;
    }

    public Long getOrg_id() {
        return org_id;
    }

    public void setOrg_id(Long org_id) {
        this.org_id = org_id;
    }

    public void setLogin_times(int login_times) {
        this.login_times = login_times;
    }

    public Set<SysRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<SysRole> roles) {
        this.roles = roles;
    }

    public String getLock_by() {
        return lock_by;
    }

    public void setLock_by(String lock_by) {
        this.lock_by = lock_by;
    }

    public byte[] getHead_img() {
        return head_img;
    }

    public void setHead_img(byte[] head_img) {
        this.head_img = head_img;
    }
}
