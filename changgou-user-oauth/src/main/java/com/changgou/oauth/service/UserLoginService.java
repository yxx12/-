package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

import java.io.UnsupportedEncodingException;

/**
 * 用户认证
 */
public interface UserLoginService {
    /**
     * 用户登录认证
     * @param username     用户名
     * @param password     密码
     * @param grant_type   授权方式
     * @param clientId     客户端id
     * @param clientSecret 客户端密钥
     * @return 认证后的信息:access_token(访问token),refresh_token(刷新token),jti(token的唯一标识)
     */
    AuthToken login(String username, String password, String grant_type, String clientId, String clientSecret) throws UnsupportedEncodingException;
}
