package csnight.redis.monitor.auth.handler;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.auth.config.JdbcTokenRepositoryExt;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.utils.RespTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private static Logger _log = LoggerFactory.getLogger(LoginSuccessHandler.class);
    private SysUserRepository sysUserRepository;
    private Map<String, String> LoginUserList = new HashMap<>();
    private JdbcTokenRepositoryExt tokenRepositoryExt;

    public Map<String, String> getLoginUserList() {
        return LoginUserList;
    }

    public LoginSuccessHandler(SysUserRepository sysUserRepository) {
        this.sysUserRepository = sysUserRepository;
    }

    public void setTokenRepositoryExt(JdbcTokenRepositoryExt tokenRepositoryExt) {
        this.tokenRepositoryExt = tokenRepositoryExt;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SysUser sysUser = sysUserRepository.findByUsername(userDetails.getUsername());
        sysUser.setLogin_times(sysUser.getLogin_times() + 1);
        sysUser.setLast_login(new Date());
        sysUserRepository.save(sysUser);
        LoginUserList.put(sysUser.getUsername(), request.getSession().getId());
        //清理冗余remember-me token
        List<PersistentRememberMeToken> extTokenForName = tokenRepositoryExt.getTokenForName(userDetails.getUsername());
        for (int i = extTokenForName.size() - 1; i > 0; i--) {
            String username = extTokenForName.get(i).getUsername();
            String token = extTokenForName.get(i).getTokenValue();
            tokenRepositoryExt.removeUserOldToken(username, token);
        }
        //跳转认证模式
        //super.setDefaultTargetUrl("/");
        //super.onAuthenticationSuccess(request, response, authentication);
        _log.info(sysUser.getUsername() + ":账户登陆成功 " + new Date());
        //JSON认证模式
        JSONObject jo_res = new JSONObject();
        jo_res.put("msg", "Login Success");
        jo_res.put("username", sysUser.getUsername());
        jo_res.put("LoginTK", IdentifyUtils.getUUID());
        response.getWriter().write(JSONObject.toJSONString(new RespTemplate(200, HttpStatus.OK, jo_res, "/auth/sign", "Login")));
    }
}
