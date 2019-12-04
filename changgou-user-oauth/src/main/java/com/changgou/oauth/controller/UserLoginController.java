package com.changgou.oauth.controller;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/user")
public class UserLoginController {

    @Autowired
    private UserLoginService userLoginService;
    //客户端ID
    @Value("${auth.clientId}")
    private String clientId;

    //秘钥
    @Value("${auth.clientSecret}")
    private String clientSecret;

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/login")
    public Result login(String username, String password, HttpServletResponse response) throws UnsupportedEncodingException {
        try {
            String grant_type = "password";
            AuthToken authToken = userLoginService.login(username, password, grant_type, clientId, clientSecret);

            String token = authToken.getAccessToken();
            Cookie cookie=new Cookie("Authorization",token);
            cookie.setDomain("localhost");              //设置cookie存储路径
            cookie.setPath("/");
            cookie.setMaxAge(90);
            response.addCookie(cookie);
            return new Result(true, StatusCode.OK,"登陆成功，正在跳转.....");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new Result(false, StatusCode.ERROR,"登陆失败");

    }
}
