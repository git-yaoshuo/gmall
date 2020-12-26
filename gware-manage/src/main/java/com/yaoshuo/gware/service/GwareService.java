package com.yaoshuo.gware.service;

import com.yaoshuo.gware.bean.WareInfo;
import com.yaoshuo.gware.bean.WareOrderTask;
import com.yaoshuo.gware.bean.WareSku;

import java.util.List;
import java.util.Map;

/**
 * @param
 * @return
 */
public interface GwareService {

    /**
     * 根据商品id获取库存剩余数量
     * @param skuid
     * @return
     */
    public Integer  getStockBySkuId(String skuid);

    /**
     * 根据要购买的商品id和商品数量，判断剩余库存是否足够
     * @param skuid
     * @param num
     * @return
     */
    public boolean  hasStockBySkuId(String skuid,Integer num);

    /**
     * 根据skuid获取库存信息列表
     * @param skuid
     * @return
     */
    public List<WareInfo> getWareInfoBySkuid(String skuid);

    /**
     * 添加库存仓库地址信息
     */
    public void addWareInfo();

    /**
     * 根据商品skuId列表获取一个库存商品map集合
     * @param skuIdList
     * @return
     */
    public Map<String,List<String>> getWareSkuMap(List<String> skuIdList);

    /**
     * 添加库存商品
     * @param wareSku
     */
    public void addWareSku(WareSku wareSku);

    public void deliveryStock(WareOrderTask taskExample) ;

    public WareOrderTask saveWareOrderTask(WareOrderTask wareOrderTask );

    public  List<WareOrderTask> checkOrderSplit(WareOrderTask wareOrderTask);

    public void lockStock(WareOrderTask wareOrderTask);

    public List<WareOrderTask> getWareOrderTaskList(WareOrderTask wareOrderTask);

    /**
     * 获取所有库存商品信息列表
     * @return
     */
    public List<WareSku> getWareSkuList();

    /**
     * 获取所有的库存信息列表
     * @return
     */
    public List<WareInfo> getWareInfoList();

    /**
     * 减库存后，同时将减库存后的状态发送到activemq中，同步给订单模块
     * @param orderId
     */
    void sendSkuDeductStatus(String orderId);

}
