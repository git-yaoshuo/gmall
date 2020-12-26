package com.yaoshuo.gmall.user;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@EnableDubbo(scanBasePackages = "com.yaoshuo.gmall.user")
@MapperScan("com.yaoshuo.gmall.user.mapper")
@ComponentScan("com.yaoshuo.gmall")
@EnableTransactionManagement
@SpringBootApplication
public class GmallUserManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallUserManageApplication.class, args);
    }

}
