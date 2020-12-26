package com.yaoshuo.gmall.payment.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "alipay")
@Component
public class AlipayConfig implements InitializingBean {

//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	// 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
	public static String app_id = "2016103000779545";

	// 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6pfFseFMZrnE5BnAz75R4qlJHt1U2GXmpyjmxi3iKFtSI2M/8oTvLgfaTBoPGDgr7VPy4ere6d1ceG2761myS/pS/BCiVhaV6S4yPNBg5ku10nj+1YWZK/bF5qR0oFXQSK380nb7M0zqPFqATXS/+N/4fGinTjlJm3Mmypu29T/zMqbAK5Vo/YgB9YaTJG7ml6yb5AN/sFBzS4ovpqplhQYO7FX2RTSQP6exUMNIAtD+8XWQD/KkvH8amGL0CPc8UAascM1TTZ9fdUnXKv550JCGHOwBBRsbsx9XGAC1AHAbdfyE1pcptOxzR8LRRVgpgppM/Bk6aqQkiSQEu69jhAgMBAAECggEAZ2bVV+VVDE4y1Ye2k18v3qVlDewIHf+BAkpRKgX9UmsRdXq/xtJAAR9PzeX3DLrs5I4Rr9X0gS3qKnjCQ96Uqd614xjXrvYqVZUTGqaeoQ6/1DJzUvXRqMvhDWheCb3Q3twQDcMyV+DReUHsjq26j9jPRMXG1DfteKTqbLnDzXNUmy4UXCE+dVnhCYL1xftpnrX4r69MfHsLaGzSmzog7vKXJ7GA7SpxQ+afgDBpnVuGloge5ffLqIHvYUJJm63U9uuW0MMffY3UXlVF3V77HQShzZtyiebbU+t65gkZs7bmnu5gzAmw6+9m+5f2S9I/KzAbMRbKEJIdYL+a8zlMkQKBgQD/MfQ93lh5qnnOqtPxy3J2IYN3OzT+f2wvrsLnUKOrw9ZVjqNOhlvugnS8thAGe0QDiMavGZ15+Smwyk11rQQbe9dALTsXyGRc+dC+/M/mj8wTdJrHada3eE0Zc+bmV0oF2wpuQC7mRPlNrQeXQB/niwA1wyL+MLiiAe9CYJGhdQKBgQC7PKTSsYVAQmFDqKDeaUWfP+aLgkINxtPFJtRd0AbOo7C7A9W0OVlkNqsCp6eTbqt4iU898ZR/4QHBa/QndKYFb9gidgR/kkGXud+Idfn7LCVaRWJVpHE3EalJmPt10ilJIqEmfkkILSJR7tDb47Uo2mEO8t+XEbV4mYPHQurgPQKBgQCGLSO4K+HlBodI7HTHmyqxhelzlUPF1AV0BLUhnjqQI0XcckTXErnfa+QpHKjMX59hJFci1ZsA+Mq804Fqr++K69A/RAjf9lMet5LT+GTvWwf36sU+uB1XFf+jtFbhIWAgOA0B7uk51WYnOyTUl7iuq10O52zMrZ1GQTIlelTXyQKBgCerQIx3GfE0XyETkAmvKhYEVtcF4PLERkfF40aA9YBARAhLVrKZV+wYfQDR/noI/4FJLiXEkh7J0DJnqdHWL/qE6LjcYjWZChaYRdjGjhD+GGfYvW3Bqk7tif1S0Sv0O9UH5jaemOCHl3bebxe3VcFzCw88u8xo7xa/B6Fm9xZNAoGAF/wIraW5fsRPFp2WREvbGv6CK3JlnI99cV5OaWyGb6hhks9vHyOYJzckWOBEjoWA3Zmu/6Bjz2OsJa5dgxao7IIOnbccfed5UCJM9H96hHDIkOilmPCA3ZRWoWIHFStfF7iajdu1TZrhJAvLgcKlNPlCvz6+HuGCuxHGtn3ugDo=";

	// 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAljyu5bumflHGpAuy4i8jG4HNNtrpE+FzfuBL0InHaraV+oZx+5xYgFEglPVf89h0jHYyEuZgZMOBf6cDBi5OLLKslDa4k23dGpakn8mAxBby1xSMlPnMHP3HvFQuvFRNljJyUvlR4mYJxlidWa3QoUIdyMKYLiT+KYmysXQamIIHe5qtlvOlLGJ0dD0CdWPfRix8RXwWk5AbbisEGzffzA0W34EEitkzPGs8KHe4GRrimGp841A8T5I3X/adviJptHFQlBdpEem5MzoaQy4psE+8XNZ5DxqSh9vSVrAp3OuFZ00EFpILJbZQrS2dAxCMdmd3Ov0FpzYmLar6ae2wAQIDAQAB";

	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String notify_url = "http://ays.free.idcfengye.com/notify_url.jsp";

	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String return_url = "http://ays.free.idcfengye.com/return_url.jsp";

	// 签名方式
	public static String sign_type = "RSA2";

	// 字符编码格式
	public static String charset = "utf-8";

	// 支付宝网关
	//public static String gatewayUrl = "https://openapi.alipay.com/gateway.do";
	public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

	public static String return_payment_url ;


	private String notify_url_1;
	private String return_url_1;
	private String return_payment_url_1;

//	private String app_id_1;
//	private String merchant_private_key_1;
//	private String alipay_public_key_1;
//	private String sign_type_1;
//	private String charset_1;
//	private String gatewayUrl_1;


	@Override
	public void afterPropertiesSet() throws Exception {
		notify_url = this.notify_url_1;
		return_url = this.return_url_1;
		return_payment_url = this.return_payment_url_1;

//		app_id = this.app_id_1;
//		merchant_private_key = this.merchant_private_key_1;
//		alipay_public_key = this.alipay_public_key_1;
//		sign_type = this.sign_type_1;
//		charset = this.charset_1;
//		gatewayUrl = this.gatewayUrl_1;
	}
}