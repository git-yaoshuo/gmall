package com.yaoshuo.gmall.service.manage;

import com.yaoshuo.gmall.bean.es.SkuLsInfo;
import com.yaoshuo.gmall.bean.es.SkuLsParams;
import com.yaoshuo.gmall.bean.es.SkuLsResult;

public interface ListService {

    /**
     * 保存商品sku信息到 elasticsearch中
     * @param skuLsInfo
     */
    void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    /**
     * 根据用户输入的查询条件，再es中查询数据信息
     * @param skuLsParams
     * @return
     */
    SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 根据商品skuId更新商品的热度评分
     * @param skuId
     */
    void updateSkuLsHotScoreBySkuId(String skuId);
}
