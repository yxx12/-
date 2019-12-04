package com.changgou.wxPay.feign;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("wxPay")
@RequestMapping("/weixin/pay")
public interface WeixinPayFeign {
    @RequestMapping("/closePay")
     Result<Map<String, String>> closePay(@RequestParam(value = "orderId") Long orderId);

}
