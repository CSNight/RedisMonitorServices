package com.csnight.redis.monitor.controller;

import com.csnight.redis.monitor.auth.service.SignUpUserService;
import com.csnight.redis.monitor.auth.jpa.SysUser;
import com.csnight.redis.monitor.auth.jpa.UserDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class LoginController {
    private final SignUpUserService signUpUserService;

    public LoginController(SignUpUserService signUpUserService) {
        this.signUpUserService = signUpUserService;
    }

    @GetMapping("/sign")
    public String sign() {
        return "sign";
    }

    @GetMapping("/register")
    public String createUser(Model model) {
        model.addAttribute("user",new UserDto());
        return "register";
    }

    @PostMapping("/sign.html?error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "sign";
    }

    @GetMapping("/userInfo")
    public String userInfo(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = userDetails.getUsername();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        model.addAttribute("name", name);
        model.addAttribute("date", date);
        return "userInfo";
    }

    @PostMapping("/register")
    public String createUser(@ModelAttribute("user") @Valid UserDto userDto, BindingResult result, Model model) {

        if (result.hasErrors()) {

            return "register";
        }
        //check name 是否已使用
        if (signUpUserService.checkUserByName(userDto.getUsername())) {
            result.rejectValue("name", "error.user", "name已使用");
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
            result.rejectValue("name", "error.user", "Name already exists");
        }

        return "redirect:/userInfo";

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
