package com.yaoshuo.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yaoshuo.gmall.bean.manage.*;
import com.yaoshuo.gmall.manage.mapper.SkuAttrValueMapper;
import com.yaoshuo.gmall.manage.mapper.SkuImageMapper;
import com.yaoshuo.gmall.manage.mapper.SkuInfoMapper;
import com.yaoshuo.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.yaoshuo.gmall.service.manage.SkuManageService;
import com.yaoshuo.gmall.util.JedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yaoshuo.gmall.constant.RedisConstant.*;

@Service
public class SkuManageServiceImpl implements SkuManageService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    /**
     * 保存skuInfo
     * @param skuInfo
     */
    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        if (skuInfo != null) {
            skuInfoMapper.insertSelective(skuInfo);

            List<SkuImage> skuImageList = skuInfo.getSkuImageList();
            if (skuImageList != null && skuImageList.size() > 0) {
                for (SkuImage skuImage : skuImageList) {
                    skuImage.setSkuId(skuInfo.getId());
                    skuImageMapper.insertSelective(skuImage);
                }
            }

            List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
            if (skuAttrValueList != null && skuAttrValueList.size() > 0) {
                for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                    skuAttrValue.setSkuId(skuInfo.getId());
                    skuAttrValueMapper.insertSelective(skuAttrValue);
                }
            }

            List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0) {
                for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                    skuSaleAttrValue.setSkuId(skuInfo.getId());
                    skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
                }
            }

        }
    }

    /**
     * 根据skuId查询商品详情信息skuInfo,使用redis缓存进行优化
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {

        /*
        //redis分布式锁的第二种解决方案：使用redisson
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.80.131:6379");

        RedissonClient redissonClient = Redisson.create(config);
        RLock lock = redissonClient.getLock("myLock");

        lock.lock(10, TimeUnit.SECONDS);

        SkuInfo skuInfo = null;
        Jedis jedis = null;

        try {

            jedis = JedisUtils.getJedis();

            String skuInfoKey = REDIS_PREFIX_SKU + skuId + REDIS_SUFFIX_SKU_INFO;

            if (jedis.exists(skuInfoKey)){
                String skuInfoStr = jedis.get(skuInfoKey);
                skuInfo = JSON.parseObject(skuInfoStr, SkuInfo.class);
            }else {
                skuInfo = getSkuInfoFromDB(skuId);
                jedis.set(skuInfoKey,JSON.toJSONString(skuInfo),"NX","PX",REDIS_STRING_SKUINFO_EXPIRE_TIME);
            }
            String skuInfoStr = jedis.get(skuInfoKey);
            return skuInfo;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }

            lock.unlock();
        }
        return getSkuInfoFromDB(skuId);*/

        //===========================================================================

        //reids分布式锁的第一种解决方案：加锁

        SkuInfo skuInfo = null;
        Jedis jedis = null;

        try {
            jedis = JedisUtils.getJedis();

            String skuInfoKey = REDIS_PREFIX_SKU + skuId + REDIS_SUFFIX_SKU_INFO;

            String skuInfoStr = jedis.get(skuInfoKey);
            if (skuInfoStr == null || skuInfoStr.length() == 0){

                //设置分布式锁lockKey
                String lockKey = REDIS_PREFIX_SKU + skuId + REDIS_SUFFIX_LOCK;
                //生成分布式锁lockValue
                String lockValue = jedis.set(lockKey, "OK", "NX", "PX", REDIS_STRING_LOCK_EXPIRE_TIME);

                if ("OK".equals(lockValue)){
                    skuInfo = getSkuInfoFromDB(skuId);
                    jedis.set(skuInfoKey, JSON.toJSONString(skuInfo),"NX","PX",REDIS_STRING_SKUINFO_EXPIRE_TIME);
                }else {
                    Thread.sleep(1000);
                    return getSkuInfoFromDB(skuId);
                }
            }else {
                skuInfo = JSON.parseObject(skuInfoStr, SkuInfo.class);
            }

            return skuInfo;

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return getSkuInfoFromDB(skuId);
    }

    private SkuInfo getSkuInfoFromDB(String skuId) {

        SkuInfo skuInfo = null;
        skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        if (skuInfo != null) {
            //设置skuInfo中的图片列表
            skuInfo.setSkuImageList(getSkuImageBySkuId(skuId));
            //设置skuInfo中的平台属性值的列表
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);

            skuInfo.setSkuAttrValueList(skuAttrValueMapper.select(skuAttrValue));
        }

        return skuInfo;
    }

    @Override
    public List<SkuImage> getSkuImageBySkuId(String skuId) {

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);

        return skuImageMapper.select(skuImage);
    }

    @Override
    public String getSkuSaleAttrValueListBySpuId(String spuId) {

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpuId(spuId);

        StringBuilder key = new StringBuilder();
        Map<String, Object> map = new HashMap<String, Object>();

        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0){
            for (int i = 0; i < skuSaleAttrValueList.size(); i++) {

                SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);

                if (key.length() > 0){
                    key.append("|");
                }
                key.append(skuSaleAttrValue.getSaleAttrValueId());

                if ((i+1) == skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                    map.put(key.toString(),skuSaleAttrValue.getSkuId());
                    key.delete(0,key.length());
                }
            }
        }

        return JSON.toJSONString(map);
    }


}
