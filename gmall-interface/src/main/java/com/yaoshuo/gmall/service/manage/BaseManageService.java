package com.yaoshuo.gmall.service.manage;

import com.yaoshuo.gmall.bean.manage.*;

import java.util.List;

public interface BaseManageService {

    /**
     * 获取一级分类列表集合
     *
     * @return
     */
    List<BaseCatalog1> getCatalog1();

    /**
     * 根据一级分类id获取二级分类的列表集合
     *
     * @param catalog1Id
     * @return
     */
    List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 根据二级分类id获取三级分类的列表集合
     *
     * @param catalog2Id
     * @return
     */
    List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级分类id获取平台属性列表集合
     *
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(String catalog3Id);


    /**
     * 添加平台属性
     *
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性id获取平台属性值列表集合
     *
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrValueList(String attrId);

    /**
     * 根据平台属性值ids获取平台属性及属性值列表
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getAttrInfoListByBaseAttrValueIds(List<String> attrValueIdList);
}
