package com.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000121660971";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDWlX0dpxzCb8rne58HcVo9sN7xKa+a/3pSaMyF/lpCoXPJ7sJgzo1k92nHH2IGsubnoJpYLRJzFHQYHC8t7ZrZfvE8CP9OYl5vwDEA0IizqnKDqonS0w3+FHlisL9VG4AeyRyjPXPBjxL9otKc8Z9bOtssNwStkuQoYhbz1vH3a0GtYBucNkAM77eX4sQYTlq36NmHAR6JS7+n8FAEb5ozA0o6QuIGXcgKGpIw423QVOYkz8EU4nOdufrMuGoDZnaJlTCW3PaMCsEsgdJekqYnmIzZick8+fE4d9Z9jVPofaQvMnxTtSM+EyirUavqZsq8e/Q59yhcB8RHZaVSh3Z5AgMBAAECggEBAJANsvyhHzF4kyJrXwtEEWywvDnx3i2TrK5+JC8f3cA1WuIJIvySLWHGpoBClTAiEdkVfAgN2gDfjkBRS2rtw7KXAaNn9tzMNNGYmE0PsXXLyyyQ5A1gGlijrgmO1iNwXsZMBKmhq4AZaIpVMEaj/nLkRBaeqMlNf1WM5BO2DKInZ/QCHMLkIPLkGjWQy3JJdWitaCoZi7D9olSeEeF8n2nlbxUXL9gJ5aPheU6D6ri8Mg0jOBDMw7j5NGaTYemNyr75lwvFcBZi3UEVROBEw+Gd8ZX07+oomwc8YSMhF7bSim00SaZhJVl3c/sSsWmZI+OAEqv+NTJ0JwqzRyr2cbECgYEA890fi5kqDXU01IKloI7g8dmFhDCkgwtFZZiPt2LQjCfBXcPxAlSiH4RENp9zepm5pPQeXGbj0mJs320ocHmkTpTFv6DgTxnSSwJ9Su9BwqZ/I+jA5fmgR4iDGAnIfpvTNHe0NtA2jQJgJDkPnRhzJGvG4a344g0LtmvUFHgsIB8CgYEA4UNVk0O5J2c4GWuGQzIqK57C8QaQzWzjBkfi5cRJSCWHpkAlY+BpiO2x4QylrzlEHuYLCn1Sdnc0i0G5rjEnHt/oVWVC2hHqVHxZcpJVxolZ6L5JcnSG5viNwCd2Ag634cnLdSRIuv4EKxdUuLOWJV5wj9u0WMjv2XMNExYANmcCgYEA6oQ/Hu/HyjNp/+g4QcTFvK+UQADLZJ6FOWPS6egkaHSe935Emxoq4yY+t0z2L/P/3QtK20+zThKLYv5Fwoyon01hMp0Sud86JhqZxWN7mSam5DCkuUUrhz9X2tklr+RI448uhvwLSeX78TnSsx/dMyxWkZfN3g3vspnV1W2P+b8CgYAol9u5+ABLq3weiJPfVBDpX2i9ynMZsb+PiDDJOwABslGpkfGq9+vMsxjU91R8KROvhcEDmOXdV0nutl9hlpX4KF1T1jWf2o6hvu7XiKtQZbwABxpxN7J/uNbCg9nuMPzOAXFaoLlCiTfzE/c+eY5bGZOrOOpc/YjLZMI8aECNjQKBgQDJu3dzJbk6wBnzdIq1tBuWEFOQXPFRCJzkEC0QevV8I1CGnMgR9PqkuPBdRV9q4ew5/nMZ/83AuPxmVhby85t9Eh/Ry5dEgb8Xl5NvCkdlf1k8sso7L4I7rg+VOUTmVfDcgo7bpgtoPRCHwn+QGH/AOr7Jb2OUkqo3IrPmvgbaUA==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkEGpU8X+Is1+7tGcknfipYsEkFhYfjiQHeJbLzaM5jL+TOoI96yV6l2qq3/ZpcjO0jWLmLYCjJdULNzF91TWvLNy8So0ZQN7F0BEkD0zlgvH968u9Ya4/6ZKfC7mKcS0ClppqACn6as4gBpFj6OCgifwuiusyDOeVVUrHMOpYxJNDqx5gj2rediUyfR7i3+g2qFUmK8Ag5FDb+rWeD0yGuv95YKhhDvFK3n625dVO2UVOMr3/kDLJ/vWlRgFdohVw5bAhdoZcP45kuTpLckEQyCJ61P2ESeeGlGoUWlfwYLTQg38+Gg2a2eedvAvPM6WfgcDLJHSi9r/EkXkq7x0QQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://order.olinmall.com/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.olinmall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        return alipayClient.pageExecute(alipayRequest).getBody();

    }
}
