package com.csnight.redis.monitor.controller;

import com.csnight.redis.monitor.auth.jpa.SysUser;
import com.csnight.redis.monitor.auth.jpa.UserDto;
import com.csnight.redis.monitor.auth.service.SignUpUserService;
import com.csnight.redis.monitor.utils.VerifyCodeUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/auth")
public class LoginController {
    private final SignUpUserService signUpUserService;

    public LoginController(SignUpUserService signUpUserService) {
        this.signUpUserService = signUpUserService;
    }

    @GetMapping("/sign")
    public String sign(HttpServletRequest request) {
        String session_id = request.getSession().getId();
        System.out.println(session_id);
        return "sign";
    }

    @GetMapping("/register")
    public String createUser(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @GetMapping("/failed")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "sign";
    }

    @GetMapping("/user_info")
    public String userInfo(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = userDetails.getUsername();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        model.addAttribute("name", name);
        model.addAttribute("date", date);
        return "user_info";
    }

    @PostMapping("/register")
    public String createUser(@ModelAttribute("user") @Valid UserDto userDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        //check name 是否已使用
        if (signUpUserService.checkUserByName(userDto.getUsername())) {
            result.rejectValue("username", "error.user", "用户已存在");
            userDto.setPassword("");
            userDto.setMatch_password("");
            userDto.setUsername("");
            model.addAttribute("user", userDto);
            return "register";
        }
        //check email 是否已注册。
        if (signUpUserService.checkUserByEmail(userDto.getEmail())) {
            userDto.setPassword("");
            userDto.setMatch_password("");
            userDto.setEmail("");
            result.rejectValue("email", "error.user", "Email已注册");
            return "register";
        }
        //check password equal
        if (!checkPassWordUniform(userDto.getPassword(), userDto.getMatch_password())) {
            userDto.setPassword("");
            userDto.setMatch_password("");
            result.rejectValue("match_password", "error.user", "两次输入密码不一致");
            return "register";
        }

        try {
            createUserAccount(userDto);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("email", "error.user", "Email已注册");
            result.rejectValue("username", "error.user", "用户已存在");
        }
        return "redirect:/auth/user_info";
    }

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
