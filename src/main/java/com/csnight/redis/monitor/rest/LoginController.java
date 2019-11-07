package com.csnight.redis.monitor.rest;

import com.csnight.redis.monitor.aop.LogBack;
import com.csnight.redis.monitor.auth.service.LoginUserService;
import com.csnight.redis.monitor.auth.service.SignUpUserService;
import com.csnight.redis.monitor.db.jpa.SysUser;
import com.csnight.redis.monitor.db.jpa.UserDto;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(value = "auth")
@Api(tags = "注册登录API")
public class LoginController {
    @Resource
    private SignUpUserService signUpUserService;
    @Resource
    private LoginUserService loginUserService;
    private static Logger _log = LoggerFactory.getLogger(com.csnight.redis.monitor.controller.auth.LoginController.class);

    @LogBack
    @ApiOperation(value = "获取用户详情")
    @RequestMapping(value = "user_info", method = RequestMethod.GET)
    public RespTemplate UserInfo(String username) {
        SysUser sysUser = loginUserService.GetUserInfo(username);
        if (sysUser == null) {
            return new RespTemplate(HttpStatus.NOT_FOUND, "failed");
        }
        sysUser.setPassword("");
        return new RespTemplate(HttpStatus.OK, sysUser);
    }

    @LogBack
    @ApiOperation(value = "用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public RespTemplate createUser(@ModelAttribute("user") @Valid UserDto userDto, BindingResult result) {
        if (result.hasErrors()) {
            _log.error(JSONUtil.object2json(result.getAllErrors()));
            return new RespTemplate(HttpStatus.BAD_REQUEST, JSONUtil.object2json(result.getAllErrors()));
        }
        //check name 是否已使用
        if (signUpUserService.checkUserByName(userDto.getUsername())) {
            result.rejectValue("username", "error.user", "用户已存在");
            userDto.setPassword("");
            userDto.setMatch_password("");
            userDto.setUsername("");
            _log.error("用户已存在");
            return new RespTemplate(HttpStatus.CONFLICT, "User has already registered!");
        }
        //check email 是否已注册。
        if (signUpUserService.checkUserByEmail(userDto.getEmail())) {
            userDto.setPassword("");
            userDto.setMatch_password("");
            userDto.setEmail("");
            result.rejectValue("email", "error.user", "Email已注册");
            _log.error("Email已注册");
            return new RespTemplate(HttpStatus.CONFLICT, "Email has already registered!");
        }
        //check password equal
        if (!checkPassWordUniform(userDto.getPassword(), userDto.getMatch_password())) {
            userDto.setPassword("");
            userDto.setMatch_password("");
            result.rejectValue("match_password", "error.user", "两次输入密码不一致");
            _log.error("两次输入密码不一致");
            return new RespTemplate(HttpStatus.NOT_ACCEPTABLE, "Password check failed!");
        }

        try {
            createUserAccount(userDto);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("email", "error.user", "Email已注册");
            result.rejectValue("username", "error.user", "用户已存在");
            _log.error(e.getMessage());
        }
        _log.info(userDto.getUsername() + ":注册成功！");
        return new RespTemplate(HttpStatus.OK, "success");
    }

    @LogBack
    @GetMapping("/code")
    public void getAuthImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        response.setHeader("Access-Control-Allow-Origin", "*");
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

    private void createUserAccount(UserDto userDto) {
        signUpUserService.registerNewAccount(userDto);
    }

    private boolean checkPassWordUniform(String passWd, String matchPassWd) {
        return passWd.equals(matchPassWd);
    }
}
