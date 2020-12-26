package com.yaoshuo.gmall.order;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan("com.yaoshuo.gmall")
@EnableDubbo(scanBasePackages = "com.yaoshuo.gmall.order")
@MapperScan("com.yaoshuo.gmall.order.mapper")
public class GmallOrderServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallOrderServicesApplication.class, args);
    }

}
