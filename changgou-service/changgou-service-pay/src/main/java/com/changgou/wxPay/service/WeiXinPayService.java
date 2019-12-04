package com.changgou.wxPay.service;

import java.util.Map;

public interface WeiXinPayService {

    /***
     * 关闭支付通道
     * @param orderId
     * @return
     */
    Map<String,String> closePay(Long orderId) throws Exception;

    /**
     * 通过订单号和支付金额生成支付通道
     *
     * @param parameters
     * @return
     */
    Map<String, String> createNative(Map<String,String> parameters);

    /**
     * 查询订单支付状态
     *
     * @param out_trade_no
     * @return
     */
    Map<String, String> queryStatus(String out_trade_no);
}
