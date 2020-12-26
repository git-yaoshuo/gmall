package com.yaoshuo.gmall.util;

import com.yaoshuo.gmall.constant.RedisConstant;
import com.yaoshuo.gmall.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class JedisUtils {

    private static JedisPool jedisPool = null;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        //设置最大连接数
        jedisPoolConfig.setMaxTotal(200);
        //设置最小空闲连接数
        jedisPoolConfig.setMinIdle(10);
        //设置最大等待时间
        jedisPoolConfig.setMaxWaitMillis(RedisConstant.MAX_WAIT_MILLIS);
        //连接用完时设置队列
        jedisPoolConfig.setBlockWhenExhausted(true);
        //获取连接时，开启自检连接是否有效
        jedisPoolConfig.setTestOnBorrow(true);

        jedisPool = new JedisPool(jedisPoolConfig, RedisConfig.REDIS_HOST,RedisConfig.REDIS_PORT,RedisConstant.CONNECTION_TIME_OUT);
    }

    public JedisPool getJedisPool(){
        return jedisPool;
    }

    /**
     * 获取一个jedis对象
     * @return
     */
    public static Jedis getJedis(){
        return jedisPool.getResource();
    }

}
