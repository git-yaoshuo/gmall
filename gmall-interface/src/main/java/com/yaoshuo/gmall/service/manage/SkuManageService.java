package com.yaoshuo.gmall.service.manage;

import com.yaoshuo.gmall.bean.manage.SkuImage;
import com.yaoshuo.gmall.bean.manage.SkuInfo;

import java.util.List;

public interface SkuManageService {

    /**
     * 保存skuInfo到数据库中
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据skuId获取商品信息skuInfo
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoBySkuId(String skuId);

    /**
     * 根据skuId获取商品图片列表
     * @param skuId
     * @return
     */
    List<SkuImage> getSkuImageBySkuId(String skuId);

    /**
     * 根据spuId获取商品销售属性列表skuSaleAttrList
     * @param spuId
     * @return
     */
    String getSkuSaleAttrValueListBySpuId(String spuId);

}
