package com.changgou.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 自定义全局 过滤器
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    // 定义常量
    private static final String AUTHORIZE_TOKEN = "Authorization";
    //登录 页面
        private static  final  String LOGIN_URL="http://localhost:9001/oauth/login";
    /**
     * 业务处理
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request、response
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 1，判断是否是不需要令牌的请求
        String url = request.getURI().getPath();
        if (URLFilter.hasAuthorize(url)) {
            // 用户登录，放行
            return chain.filter(exchange);
        }
        // 2，判断其他条件
        // 2.1从请求参数获取token
        String token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        if (StringUtils.isEmpty(token)) {
            // 2.2 从请求头获取token
            token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        }

        if (StringUtils.isEmpty(token)) {
            // 2.3 从cookie中获取token
            HttpCookie cookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (cookie != null) {
                token = cookie.getValue();
            }
        }
        // 3 如果没有token，不放行
        if (StringUtils.isEmpty(token)) {
            response.setStatusCode(HttpStatus.SEE_OTHER); //设置响应状态码,重定向到另一个url
            String path=LOGIN_URL+"?from="+request.getURI().toString();
            response.getHeaders().add("location",path);
            return response.setComplete();
        }

        // 4 token存在,这儿不解析，添加信息头，让微服务自己去解析
        try {
//            Claims claims = JwtUtil.parseJWT(token);
            //手动添加 信息头
            request.mutate().header("Authorization", "bearer"+token);
        } catch (Exception e) {
            e.printStackTrace();
            //解析失败
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        return chain.filter(exchange);
    }

    /**
     * 过滤器的执行顺序
     * 0 最早
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
