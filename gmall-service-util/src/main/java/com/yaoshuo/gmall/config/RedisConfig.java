package com.yaoshuo.gmall.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisConfig implements InitializingBean {

    @Value("${spring.redis.host:disable}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    public static String REDIS_HOST;
    public static int REDIS_PORT;
    public static int REDIS_DATABASE;

    @Override
    public void afterPropertiesSet() throws Exception {
        REDIS_DATABASE = this.database;
        REDIS_HOST = this.host;
        REDIS_PORT = this.port;
    }

}
