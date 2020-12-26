package com.yaoshuo.gmall.cart.mapper;

import com.yaoshuo.gmall.bean.cart.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
    /**
     * 根据userId获取cartInfo集合，同时按照id进行排序，查出最新的价格
     * @param userId
     * @return
     */
    List<CartInfo> selectCartInfoWithCurrentPriceByUserId(String userId);
}
