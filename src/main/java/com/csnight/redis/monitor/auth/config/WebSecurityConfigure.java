package com.csnight.redis.monitor.auth.config;

import com.csnight.redis.monitor.auth.handler.CusLogoutSuccessHandler;
import com.csnight.redis.monitor.auth.handler.LoginSuccessHandler;
import com.csnight.redis.monitor.auth.handler.SignOutHandler;
import com.csnight.redis.monitor.auth.handler.ValidationHandler;
import com.csnight.redis.monitor.busi.sys.LoginUserService;
import com.csnight.redis.monitor.db.jpa.SysUser;
import com.csnight.redis.monitor.db.repos.SysUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {
    @Resource
    private AuthenticationFailureHandler loginFailureHandler;
    @Resource
    private AuthenticationSuccessHandler loginSuccessHandler;
    @Resource
    private CusLogoutSuccessHandler logoutSuccessHandler;
    @Resource
    private DataSource dataSource;
    @Resource
    private SysUserRepository sysUserRepository;
    @Resource
    private ValidationHandler validationHandler;
    @Resource
    private SignOutHandler signOutHandler;

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
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry
                = http.authorizeRequests();
        registry.requestMatchers(CorsUtils::isPreFlightRequest).permitAll();//让Spring security放行所有preflight request
        http.csrf().disable().authorizeRequests().antMatchers(
                "/static/**",
                "/auth/register",
                "/auth/code").permitAll() //访问允许静态文件
                .anyRequest().authenticated()
                .and().addFilterBefore(validationHandler, UsernamePasswordAuthenticationFilter.class)
                .formLogin().loginProcessingUrl("/auth/sign").successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler).and()
                .logout().logoutUrl("/auth/logout").addLogoutHandler(signOutHandler).logoutSuccessHandler(logoutSuccessHandler).permitAll()
                .and().rememberMe().tokenRepository(jdbcTokenRepositoryExt).tokenValiditySeconds(60 * 60 * 24 * 7);
        http.sessionManagement().maximumSessions(1);
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
