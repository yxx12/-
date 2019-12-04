package com.changgou.wxPay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.wxPay.service.WeiXinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeiXinPayService {
    @Value("${weixin.appid}")
    private String appid;           // 微信公众账号或开放平台APP的唯一标识

    @Value("${weixin.partner}")
    private String partner;         // 商户号

    @Value("${weixin.partnerkey}")
    private String partnerkey;      // 商户密钥

    @Value("${weixin.notifyurl}")
    private String notifyurl;       // 回调地址
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    /***
     * 关闭支付通道
     * @param orderId
     * @return
     */
    @Override
    public Map<String, String> closePay(Long orderId) throws Exception {
        //参数设置
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", appid);        //公众号id
        paramMap.put("mch_id", partner);      //商户编号
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
        paramMap.put("out_trade_no", String.valueOf(orderId));   //商家的订单号
        //签名，并将map转化为xml
        String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";
        //发送请求
        HttpClient httpClient = new HttpClient(url);
        httpClient.setHttps(true);
        //提交参数
        httpClient.setXmlParam(xmlParam);
        //提交
        httpClient.post();

        //获取返回数据
        String content = httpClient.getContent();

        //将返回数据解析成Map
        return WXPayUtil.xmlToMap(content);
    }

    @Override
    public Map<String, String> createNative(Map<String, String> parameters) {
        try {
            //微信统一下单地址：
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            //封装接口所需参数
            Map<String, String> map = new HashMap<>();
            map.put("appid", appid);                 //公众账号ID	appid
            map.put("mch_id", partner);              //商户号	mch_id
            map.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串	nonce_str
            map.put("body", "畅购一分钱学习");        //商品描述，会显示在扫面二维码支付页面
            map.put("out_trade_no", parameters.get("out_trade_no"));   //商户订单号	out_trade_no
            map.put("total_fee", parameters.get("total_fee"));         //标价金额
            map.put("spbill_create_ip", "192.168.64.59"); //终端IP	spbill_create_ip
            map.put("notify_url", notifyurl);         //通知地址
            map.put("trade_type", "NATIVE");           //交易类型
            //添加附加数据：交换机，路由器，用户名
            Map<String, String> attachMap = new HashMap<>();
            attachMap.put("exchange", parameters.get("exchange"));
            attachMap.put("routingKey", parameters.get("routingKey"));
            attachMap.put("username", parameters.get("username"));
            //添加到支付接口的参数中
            map.put("attach", JSON.toJSONString(attachMap));
            String signedXml = WXPayUtil.generateSignedXml(map, partnerkey);//签名+map转换成xml

            //参数封装完成，httpclient调用接口发请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setXmlParam(signedXml);  // 请求参数    下面的顺序不能变，最后post就发起请求了。
            httpClient.setHttps(true);          // 请求方式
            httpClient.post();
            //获取请求返回结果
            String content = httpClient.getContent();     //此处是一个xml格式的string数据
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
            //把商品信息,订单号,价格返回给支付页面
            xmlToMap.put("total_fee", parameters.get("total_fee"));
            xmlToMap.put("out_trade_no", parameters.get("out_trade_no"));

            //这里面有下的那时候传进去的好多参数，发给mq，的超时监听队列
            String xmlString = JSON.toJSONString(map);
            /**
             * 向MQ发送一个延时队列信息，包含订单消息,
             * 队列里面发string 类型消息
             * 此处第二个参数，写的是object，但是只能发string
             */
            rabbitTemplate.convertAndSend(env.getProperty("mq.pay.queue.seckillordertimerdelay"), (Object) xmlString, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setExpiration("10000");
                    System.out.println("下单发送Mq消息：" + new Date());
                    return message;
                }
            });
            System.out.println("下单数据：" + xmlToMap);
            return xmlToMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, String> queryStatus(String out_trade_no) {
        try {
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            Map<String, String> map = new HashMap<>();
            map.put("appid", appid);                 //公众账号ID	appid
            map.put("mch_id", partner);              //商户号	mch_id
            map.put("out_trade_no", out_trade_no);    //商户订单号
            map.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串	nonce_str
            String signedXml = WXPayUtil.generateSignedXml(map, partnerkey);//签名+map转换成xml

            //参数封装完成，httpclient调用接口发请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setXmlParam(signedXml);  // 请求参数    下面的顺序不能变，最后post就发起请求了。
            httpClient.setHttps(true);          // 请求方式
            httpClient.post();
            //获取请求返回结果
            String content = httpClient.getContent();
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
            return xmlToMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
