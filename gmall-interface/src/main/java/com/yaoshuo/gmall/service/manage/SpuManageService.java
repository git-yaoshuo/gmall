package com.yaoshuo.gmall.service.manage;

import com.yaoshuo.gmall.bean.manage.BaseSaleAttr;
import com.yaoshuo.gmall.bean.manage.SpuImage;
import com.yaoshuo.gmall.bean.manage.SpuInfo;
import com.yaoshuo.gmall.bean.manage.SpuSaleAttr;

import java.util.List;

public interface SpuManageService {

    /**
     * 根据三级分类id获取商品信息集合
     *
     * @param catalog3Id
     * @return
     */
    List<SpuInfo> getSpuList(String catalog3Id);

    /**
     * 获取基本商品销售属性列表
     *
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存商品spu相关信息
     *
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据商品id获取商品销售属性列表
     *
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 根据商品id获取商品图片列表
     *
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(String spuId);

    /**
     * 根据spuId和skuId获取商品销售列表--详情中
     * @param spuId
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListBySpuIdAndSkuId(String spuId, String skuId);

}
