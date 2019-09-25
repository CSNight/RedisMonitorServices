package com.csnight.redis.monitor.auth.config;

import com.csnight.redis.monitor.auth.repos.SysUserRepository;
import com.csnight.redis.monitor.auth.service.CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {
    private final AuthenticationFailureHandler loginFailureHandler;
    private final AuthenticationSuccessHandler loginSuccessHandler;
    private final SignOutHandler signOutHandler;
    private final DataSource dataSource;
    private final SysUserRepository sysUserRepository;

    public WebSecurityConfigure(DataSource dataSource,
                                SysUserRepository sysUserRepository,
                                AuthenticationFailureHandler loginFailureHandler,
                                AuthenticationSuccessHandler loginSuccessHandler,
                                SignOutHandler signOutHandler) {
        this.dataSource = dataSource;
        this.sysUserRepository = sysUserRepository;
        this.loginFailureHandler = loginFailureHandler;
        this.loginSuccessHandler = loginSuccessHandler;
        this.signOutHandler = signOutHandler;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(new CustomUserService(sysUserRepository)).passwordEncoder(passwordEncoder()); //user Details Service验证
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests().antMatchers("/static/**", "/css/**").permitAll() //访问允许静态文件
                .antMatchers("/","/auth/failed", "/auth/register").permitAll().anyRequest().authenticated()
                .and().formLogin().loginPage("/auth/sign").successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)//指定登录页和登录失败页
                .and().logout().logoutUrl("/auth/logout").logoutSuccessUrl("/auth/sign").addLogoutHandler(signOutHandler).permitAll()
                .and().rememberMe().tokenRepository(tokenRepository()).tokenValiditySeconds(60);
    }

    private PersistentTokenRepository tokenRepository() {
        //存储内存，不推荐
        // InMemoryTokenRepositoryImpl memory =new InMemoryTokenRepositoryImpl();
        // return memory;
        /* 存档到数据库中 **/
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(this.dataSource);
        return db;
    }
}
