package com.csnight.redis.monitor.auth.jpa;

import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "persistent_logins")
public class PersistentLogins {
    @Id
    @Column(name = "username", length = 64)
    private String username;
    @Column(name = "series", length = 64)
    private String series;
    @Column(name = "token", length = 64)
    private String token;
    @Column(name = "last_used")
    @Temporal(TemporalType.TIMESTAMP)
    private Date last_used;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getLast_used() {
        return last_used;
    }

    public void setLast_used(Date last_used) {
        this.last_used = last_used;
    }
}
