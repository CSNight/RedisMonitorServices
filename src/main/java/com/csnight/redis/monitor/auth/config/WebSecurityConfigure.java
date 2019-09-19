package com.csnight.redis.monitor.auth.config;

import com.csnight.redis.monitor.auth.impl.SysUserMapper;
import com.csnight.redis.monitor.auth.jpa.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(new CustomUserService(sysUserMapper)).passwordEncoder(new BCryptPasswordEncoder()); //user Details Service验证
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  //csrf不可用
                .authorizeRequests()
                .antMatchers("/static/**", "/css/**").permitAll() //访问允许静态文件
                .antMatchers("/", "/log", "/register").permitAll() //允许访问首页和注册页
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/sign")//指定登录页和登录失败页
                .defaultSuccessUrl("/userInfo") //登录成功跳转页
                .usernameParameter("username")
                .passwordParameter("password")
                .and()
                .logout().logoutSuccessUrl("/sign").permitAll() //退出登录跳转页
                .and()
                .rememberMe() //remember me
                .tokenRepository(tokenRepository()) //存储
                .tokenValiditySeconds(24 * 60 * 60).and()
                .csrf().disable();//token有效期24h


    }

    public PersistentTokenRepository tokenRepository() {
        //存储内存，不推荐
//        InMemoryTokenRepositoryImpl memory =new InMemoryTokenRepositoryImpl();
//        return memory;
        /** 存档到数据库中 **/
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(this.dataSource);
        return db;
    }
}
