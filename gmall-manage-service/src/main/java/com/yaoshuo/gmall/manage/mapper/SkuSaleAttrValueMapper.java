package com.yaoshuo.gmall.manage.mapper;

import com.yaoshuo.gmall.bean.manage.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    /**
     * 根据spuId获取SKU销售属性值列表集合
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpuId(String spuId);

}
