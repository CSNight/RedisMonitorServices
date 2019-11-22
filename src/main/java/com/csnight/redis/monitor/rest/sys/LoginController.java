package com.csnight.redis.monitor.rest.sys;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.aop.LogAsync;
import com.csnight.redis.monitor.busi.sys.LoginUserService;
import com.csnight.redis.monitor.busi.sys.SignUpUserService;
import com.csnight.redis.monitor.db.jpa.SysUser;
import com.csnight.redis.monitor.db.repos.SysLogRepository;
import com.csnight.redis.monitor.rest.sys.dto.UserSignDto;
import com.csnight.redis.monitor.utils.BaseUtils;
import com.csnight.redis.monitor.utils.JSONUtil;
import com.csnight.redis.monitor.utils.RespTemplate;
import com.csnight.redis.monitor.utils.VerifyCodeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "auth")
@Api(tags = "注册登录API")
public class LoginController {
    @Resource
    private SignUpUserService signUpUserService;
    @Resource
    private LoginUserService loginUserService;
    private static Logger _log = LoggerFactory.getLogger(LoginController.class);

    @LogAsync
    @ApiOperation(value = "获取用户详情")
    @RequestMapping(value = "user_info", method = RequestMethod.GET)
    public RespTemplate UserInfo(String username) {
        if (username == null || username.equals("")) {
            username = BaseUtils.GetUserFromContext();
        }
        SysUser sysUser = loginUserService.GetUserInfo(username);
        if (sysUser == null) {
            return new RespTemplate(HttpStatus.NOT_FOUND, "failed");
        }
        return new RespTemplate(HttpStatus.OK, sysUser);
    }

    @LogAsync
    @ApiOperation(value = "获取用户头像")
    @RequestMapping(value = "user_avatar", method = RequestMethod.GET)
    public RespTemplate GetHeader(String username) {
        if (username == null || username.equals("")) {
            username = BaseUtils.GetUserFromContext();
        }
        SysUser sysUser = loginUserService.GetUserInfo(username);
        if (sysUser == null) {
            return new RespTemplate(HttpStatus.NOT_FOUND, "failed");
        }
        return new RespTemplate(HttpStatus.OK, new String(sysUser.getHead_img()));
    }

    @LogAsync
    @ApiOperation(value = "用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public RespTemplate createUser(@ModelAttribute("user") @Valid UserSignDto userSignDto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, List<String>> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                if (errors.containsKey(error.getField())) {
                    errors.get(error.getField()).add(error.getDefaultMessage());
                } else {
                    List<String> err = new ArrayList<>();
                    err.add(error.getDefaultMessage());
                    errors.put(error.getField(), err);
                }
            }
            _log.error(JSONUtil.object2json(errors));
            return new RespTemplate(HttpStatus.BAD_REQUEST, errors);
        }
        //check name 是否已使用
        if (signUpUserService.checkUserByName(userSignDto.getUsername())) {
            result.rejectValue("username", "error.user", "用户已存在");
            userSignDto.setPassword("");
            userSignDto.setMatch_password("");
            userSignDto.setUsername("");
            _log.error("用户已存在");
            return new RespTemplate(HttpStatus.CONFLICT, "User has already registered!");
        }
        //check email 是否已注册。
        if (signUpUserService.checkUserByEmail(userSignDto.getEmail())) {
            userSignDto.setPassword("");
            userSignDto.setMatch_password("");
            userSignDto.setEmail("");
            result.rejectValue("email", "error.user", "Email已注册");
            _log.error("Email已注册");
            return new RespTemplate(HttpStatus.CONFLICT, "Email has already registered!");
        }
        //check password equal
        if (!checkPassWordUniform(userSignDto.getPassword(), userSignDto.getMatch_password())) {
            userSignDto.setPassword("");
            userSignDto.setMatch_password("");
            result.rejectValue("match_password", "error.user", "两次输入密码不一致");
            _log.error("两次输入密码不一致");
            return new RespTemplate(HttpStatus.NOT_ACCEPTABLE, "Password check failed!");
        }

        try {
            createUserAccount(userSignDto);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("email", "error.user", "Email已注册");
            result.rejectValue("username", "error.user", "用户已存在");
            _log.error(e.getMessage());
        }
        _log.info(userSignDto.getUsername() + ":注册成功！");
        JSONObject jo_res = new JSONObject();
        jo_res.put("msg", "success");
        jo_res.put("username", userSignDto.getUsername());
        return new RespTemplate(HttpStatus.OK, jo_res);
    }

    @LogAsync
    @GetMapping("/code")
    public void getAuthImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        // 生成随机字串
        String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
        // 存入会话session
        HttpSession session = request.getSession(true);
        session.setAttribute("ValCode", verifyCode.toLowerCase());
        // 生成图片
        int w = 100, h = 46;
        VerifyCodeUtils.outputImage(w, h, response.getOutputStream(),
                verifyCode);
    }

    private void createUserAccount(UserSignDto userSignDto) {
        signUpUserService.registerNewAccount(userSignDto);
    }

    private boolean checkPassWordUniform(String passWd, String matchPassWd) {
        return passWd.equals(matchPassWd);
    }
}
