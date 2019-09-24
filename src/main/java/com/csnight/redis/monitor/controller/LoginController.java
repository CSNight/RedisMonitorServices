package com.csnight.redis.monitor.controller;

import com.csnight.redis.monitor.auth.jpa.SysUser;
import com.csnight.redis.monitor.auth.jpa.UserDto;
import com.csnight.redis.monitor.auth.service.SignUpUserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller(value = "/auth")
public class LoginController {
    private final SignUpUserService signUpUserService;

    public LoginController(SignUpUserService signUpUserService) {
        this.signUpUserService = signUpUserService;
    }

    @GetMapping("/auth/sign")
    public String sign() {
        return "sign";
    }

    @GetMapping("/auth/register")
    public String createUser(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @GetMapping("/auth/failed")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "sign";
    }

    @GetMapping("/auth/userInfo")
    public String userInfo(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = userDetails.getUsername();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        model.addAttribute("name", name);
        model.addAttribute("date", date);
        return "userInfo";
    }

    @PostMapping("/auth/register")
    public String createUser(@ModelAttribute("user") @Valid UserDto userDto, BindingResult result, Model model) {

        if (result.hasErrors()) {

            return "register";
        }
        //check name 是否已使用
        if (signUpUserService.checkUserByName(userDto.getUsername())) {
            result.rejectValue("username", "error.user", "name已使用");
            return "register";
        }
        //check email 是否已注册。
        if (signUpUserService.checkUserByEmail(userDto.getEmail())) {
            result.rejectValue("email", "error.user", "Email已注册");
            return "register";
        }
        //check password equal
        if (!checkPassWordUniform(userDto.getPassword(), userDto.getMatch_password())) {
            result.rejectValue("password", "error.user", "两次输入密码不一致");
            return "register";
        }

        try {
            createUserAccount(userDto);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("email", "error.user", "Email already exists.");
            result.rejectValue("username", "error.user", "Name already exists");
        }

        return "redirect:/auth/userInfo";

    }

    private SysUser createUserAccount(UserDto userDto) {
        SysUser registered = null;
        registered = signUpUserService.registerNewAccount(userDto);
        return registered;
    }

    private boolean checkPassWordUniform(String passWd, String matchPassWd) {
        return passWd.equals(matchPassWd);
    }
}
