package com.csnight.redis.monitor.auth.config;

import com.csnight.redis.monitor.auth.handler.LoginSuccessHandler;
import com.csnight.redis.monitor.auth.handler.SignOutHandler;
import com.csnight.redis.monitor.auth.handler.ValidationHandler;
import com.csnight.redis.monitor.db.jpa.SysUser;
import com.csnight.redis.monitor.db.repos.SysUserRepository;
import com.csnight.redis.monitor.auth.service.LoginUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {
    private final AuthenticationFailureHandler loginFailureHandler;
    private final AuthenticationSuccessHandler loginSuccessHandler;
    private final SignOutHandler signOutHandler;
    private final DataSource dataSource;
    private final SysUserRepository sysUserRepository;
    private final ValidationHandler validationHandler;

    public WebSecurityConfigure(DataSource dataSource,
                                SysUserRepository sysUserRepository,
                                AuthenticationFailureHandler loginFailureHandler,
                                AuthenticationSuccessHandler loginSuccessHandler,
                                SignOutHandler signOutHandler,
                                ValidationHandler validationHandler) {
        this.dataSource = dataSource;
        this.sysUserRepository = sysUserRepository;
        this.loginFailureHandler = loginFailureHandler;
        this.loginSuccessHandler = loginSuccessHandler;
        this.signOutHandler = signOutHandler;
        this.validationHandler = validationHandler;
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(new LoginUserService(sysUserRepository)).passwordEncoder(passwordEncoder()); //user Details Service验证
        AutoUnlockFailAccount();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JdbcTokenRepositoryExt jdbcTokenRepositoryExt = tokenRepository();
        ((LoginSuccessHandler) loginSuccessHandler).setTokenRepositoryExt(jdbcTokenRepositoryExt);
        http.csrf().disable().authorizeRequests().antMatchers(
                "/static/**","/csrf",
                "/css/**",
                "/js/**",
                "/vendor/**",
                "/img/**",
                "/",
                "/404",
                "/403",
                "/500",
                "/auth/failed",
                "/auth/register",
                "/auth/code").permitAll() //访问允许静态文件
                .anyRequest().authenticated()
                .and().addFilterBefore(validationHandler, UsernamePasswordAuthenticationFilter.class)
                .formLogin().loginPage("/auth/sign").successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler).and().exceptionHandling().accessDeniedPage("/403")//指定登录页和登录失败页
                .and().logout().logoutUrl("/auth/logout").logoutSuccessUrl("/auth/sign").addLogoutHandler(signOutHandler).permitAll()
                .and().rememberMe().tokenRepository(jdbcTokenRepositoryExt).tokenValiditySeconds(60 * 60 * 24 * 7);
        http.sessionManagement().maximumSessions(1).expiredUrl("/auth/sign");
        http.headers().frameOptions().sameOrigin();
    }

    @Bean
    public JdbcTokenRepositoryExt tokenRepository() {
        //存储内存，不推荐
        // InMemoryTokenRepositoryImpl memory =new InMemoryTokenRepositoryImpl();
        // return memory;
        /* 存档到数据库中 **/
        JdbcTokenRepositoryExt db = new JdbcTokenRepositoryExt();
        db.setDataSource(this.dataSource);
        return db;
    }


    private void AutoUnlockFailAccount() {
        List<SysUser> lockedUser = sysUserRepository.findAllByEnabled(false);
        for (SysUser locked : lockedUser) {
            if (locked.getLock_by().equals("lockByFails")) {
                locked.setEnabled(true);
                locked.setLock_by("none");
                sysUserRepository.save(locked);
            }
        }
    }
}
