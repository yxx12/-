package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Base64;
import java.util.Map;

@Service
public class UserLoginServiceImpl implements UserLoginService {

    @Autowired
    private RestTemplate restTemplate;

    //可以获取服务的地址，name
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * 用户登录认证
     *
     * @param username     用户名
     * @param password     密码
     * @param grant_type   授权方式
     * @param clientId     客户端id
     * @param clientSecret 客户端密钥
     * @return 认证后的信息:access_token(访问token),refresh_token(刷新token),jti(token的唯一标识)
     */
    @Override
    public AuthToken login(String username, String password, String grant_type, String clientId, String clientSecret) throws UnsupportedEncodingException {
        //生成令牌的url
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");//获取微服务的实例
        URI uri = serviceInstance.getUri();
        String url = uri + "/oauth/token";
        //封装请求体
        LinkedMultiValueMap body = new LinkedMultiValueMap();
        body.add("grant_type", grant_type);
        body.add("username", username);
        body.add("password", password);
        //封装请求头
        LinkedMultiValueMap header = new LinkedMultiValueMap();
        byte[] encode = Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes());
        String s=new String(encode, "utf-8");
        header.add("Authorization", "Basic " + s);
        HttpEntity requestEntity = new HttpEntity(body, header);

        //restTemplate调用url对应的服务
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        //获取响应体的数据
        Map<String, Object> map = responseEntity.getBody();
        AuthToken authToken=new AuthToken();
        authToken.setAccessToken((String) map.get("access_token"));
        authToken.setRefreshToken((String) map.get("refresh_token"));
        authToken.setJti((String) map.get("jti"));
        return authToken;
    }
}
