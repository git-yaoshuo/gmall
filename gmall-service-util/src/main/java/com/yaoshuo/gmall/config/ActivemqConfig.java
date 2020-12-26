package com.yaoshuo.gmall.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.Session;

@Configuration
public class ActivemqConfig {

    @Value("${spring.activemq.broker-url:disabled}")
    private String brokerURL ;

    @Value("${activemq.listener.enable:disabled}")
    private String listenerEnable;

    /**
     * 获取一个activemqConnectionFactory
     * @return
     */
    @Bean(name = "activeMQConnectionFactory")
    public ActiveMQConnectionFactory getActiveMQConnectionFactory(){
       return new ActiveMQConnectionFactory(brokerURL);
    }

    /**
     * 配置一个默认的jms监听容器工厂
     * @param activeMQConnectionFactory
     * @return
     */
    @Bean(name = "jmsListenerFactory")
    public DefaultJmsListenerContainerFactory getDefaultJmsListenerContainerFactory(@Qualifier("activeMQConnectionFactory") ActiveMQConnectionFactory activeMQConnectionFactory){
        DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();

        //设置连接工厂
        defaultJmsListenerContainerFactory.setConnectionFactory(activeMQConnectionFactory);

        //是否设置事务
        defaultJmsListenerContainerFactory.setSessionTransacted(false);
        //设置手动签收
        defaultJmsListenerContainerFactory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        //设置并发数
        defaultJmsListenerContainerFactory.setConcurrency("5");
        //设置重新连接时间间隔
        defaultJmsListenerContainerFactory.setRecoveryInterval(5000L);

        return defaultJmsListenerContainerFactory;
    }
}
