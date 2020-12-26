package com.yaoshuo.gmall.payment;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.yaoshuo.gmall")
@EnableDubbo(scanBasePackages = "com.yaoshuo.gmall.payment")
public class GmallPaymentWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPaymentWebApplication.class, args);
    }

}
