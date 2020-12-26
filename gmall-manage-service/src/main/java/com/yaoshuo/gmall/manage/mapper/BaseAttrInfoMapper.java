package com.yaoshuo.gmall.manage.mapper;

import com.yaoshuo.gmall.bean.manage.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    List<BaseAttrInfo> selectAttrInfoListByBaseAttrValueIds(List<String> attrValueIdList);
}
