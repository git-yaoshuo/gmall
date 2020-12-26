package com.yaoshuo.gmall.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

public class ActivemqUtil {

    private static PooledConnectionFactory pooledConnectionFactory = null;

    static {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.80.131:61616");
        pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);

        //设置超时时间
        pooledConnectionFactory.setExpiryTimeout(2000L);
        //设置异常是重新连接
        pooledConnectionFactory.setReconnectOnException(true);
        //设置最大连接数
        pooledConnectionFactory.setMaxConnections(5);
    }

    /**
     * 获取一个连接
     * @return
     */
    public static Connection getConnection(){

        Connection connection = null;
        try {
            connection = pooledConnectionFactory.createConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 释放资源
     * @param producer
     * @param session
     * @param connection
     */
    public static void close(MessageProducer producer, Session session, Connection connection){
        if (producer != null){
            try {
                producer.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        if (session != null){
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        if (connection != null){
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

}
