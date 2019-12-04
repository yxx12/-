package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/oauth")
public class LoginRedirect {

    /**
     * 跳转用户登录页面
     * @return
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "from",required = false)String from, Model model) {
        model.addAttribute("from",from);
        return "login";
    }
}
