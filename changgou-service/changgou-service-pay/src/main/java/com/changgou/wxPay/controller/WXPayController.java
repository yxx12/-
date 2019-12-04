package com.changgou.wxPay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.wxPay.service.WeiXinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@RestController
@RequestMapping("/weixin/pay")
public class WXPayController {
    @Autowired
    private WeiXinPayService weiXinPayService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private Environment env;

    /**
     * 支付链接入口
     *
     * @param parameters
     * @return
     */
    @RequestMapping("/createNative")
    public Result createNative(@RequestParam Map<String, String> parameters) {
        Map<String, String> map = weiXinPayService.createNative(parameters);
        if (map == null) {
            return new Result(false, StatusCode.ERROR, "下单失败");
        }
        return new Result(true, StatusCode.OK, "下单成功", map);
    }

    /**
     * 手动查询订单支付状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryStatus")
    public Result queryStatus(String out_trade_no,
                              @RequestParam(value = "exchange") String exchange,
                              @RequestParam(value = "routingKey") String routingKey) {
        Map<String, String> map = weiXinPayService.queryStatus(out_trade_no);
        //将回调数据发送mq
        rabbitTemplate.convertAndSend(env.getProperty(exchange), env.getProperty(routingKey), JSON.toJSONString(map));
        return new Result(true, StatusCode.OK, "支付状态查询成功", map);
    }

    /**
     * 回调响应订单支付信息
     *
     * @param request
     * @return
     */
    @RequestMapping("/notify/url")
    public String notifyUrl(HttpServletRequest request) throws Exception {
        //获取回调的url，获取请求的二进制数据
        ServletInputStream inputStream = request.getInputStream();
        //创建字节数组刘对象数据写道内存
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024 * 4];
        int len = 0;
        while ((len = inputStream.read(bytes)) != -1) {
            os.write(bytes, 0, len);
        }
        os.flush();
        os.close();
        inputStream.close();
        //将字节转成string
        String strXMl = new String(os.toByteArray(), "utf-8");
        Map<String, String> map = WXPayUtil.xmlToMap(strXMl);  //回调数据
        System.out.println("微信回调支付状态：" + map);
        //获取附加数据
        String attach = map.get("attach");
        Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
        // 获取路由器、交换机
        String exchange = env.getProperty(attachMap.get("exchange"));
        String routingKey = env.getProperty(attachMap.get("routingKey"));
        //将回调数据发送mq
        rabbitTemplate.convertAndSend(exchange, routingKey, JSON.toJSONString(map));
        return "success";
    }

    /***
     * 关闭支付通道
     * @param orderId
     * @return
     */
    @RequestMapping("/closePay")
    public Result<Map<String, String>> closePay(@RequestParam(value = "orderId")Long orderId) {
        try {
            Map<String, String> map = weiXinPayService.closePay(orderId);
            return new Result<>(true, StatusCode.OK, "订单关闭", map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(true, StatusCode.OK, "订单关闭失败");
        }
    }
}














