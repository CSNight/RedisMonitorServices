package com.csnight.redis.monitor.controller;

import com.csnight.redis.monitor.auth.config.CustomUserService;
import com.csnight.redis.monitor.auth.impl.SysUserMapper;
import com.csnight.redis.monitor.auth.impl.UserDto;
import com.csnight.redis.monitor.auth.jpa.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class LoginController {

    @Autowired
    private SysUserMapper userService;

    @GetMapping("/userInfo")
    public String userInfo(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = userDetails.getUsername();

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        model.addAttribute("name", name);
        model.addAttribute("date", date);
        return "userInfo";
    }

}
