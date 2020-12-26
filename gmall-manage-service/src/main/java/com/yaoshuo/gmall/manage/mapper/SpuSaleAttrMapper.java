package com.yaoshuo.gmall.manage.mapper;

import com.yaoshuo.gmall.bean.manage.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);

    List<SpuSaleAttr> selectSpuSaleAttrListBySpuIdAndSkuId(@Param("spuId") String spuId, @Param("skuId") String skuId);

}
