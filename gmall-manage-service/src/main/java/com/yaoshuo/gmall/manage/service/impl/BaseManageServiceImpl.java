package com.yaoshuo.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yaoshuo.gmall.bean.manage.*;
import com.yaoshuo.gmall.manage.mapper.*;
import com.yaoshuo.gmall.service.manage.BaseManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BaseManageServiceImpl implements BaseManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    /**
     * 获取商品一级分类列表
     * @return
     */
    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    /**
     * 根据商品一级分类catalog1Id获取商品二级分类列表
     * @param catalog1Id
     * @return
     */
    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {

        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);

        return baseCatalog2Mapper.select(baseCatalog2);
    }

    /**
     * 根据商品二级分类catalog2Id获取商品三级分类列表
     * @param catalog2Id
     * @return
     */
    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {

        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);

        return baseCatalog3Mapper.select(baseCatalog3);
    }

    /**
     * 根据商品三级分类catalog3Id获取商品基本平台属性列表
     * @param catalog3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {

        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);

        ArrayList<BaseAttrInfo> baseAttrInfos = new ArrayList<>();
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);
        if (baseAttrInfoList != null && baseAttrInfoList.size() > 0) {
            for (BaseAttrInfo attrInfo : baseAttrInfoList) {
                BaseAttrInfo baseAttr = getAttrValueList(attrInfo.getId());
                baseAttrInfos.add(baseAttr);
            }
        }

        return baseAttrInfos;
    }

    /**
     * 保存商品基本平台属性到数据库
     * @param baseAttrInfo
     */
    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        if (StringUtils.isEmpty(baseAttrInfo.getId())) {

            if (StringUtils.isEmpty(baseAttrInfo.getAttrName())) {
                throw new RuntimeException("平台属性不能为空");
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);

        } else {
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }

        //清空原有平台属性值
        BaseAttrValue baseAttrValueDel = new BaseAttrValue();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        if (attrValueList != null && attrValueList.size() > 0) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    /**
     * 根据商品基本属性attrId获取商品基本平台属性值列表
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getAttrValueList(String attrId) {

        if (StringUtils.isEmpty(attrId)) {
            return null;
        }

        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);

        baseAttrInfo.setAttrValueList(attrValueList);

        return baseAttrInfo;
    }

    /**
     * 根据平台属性值ids获取平台属性及属性值列表
     * @param attrValueIdList
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoListByBaseAttrValueIds(List<String> attrValueIdList) {

        System.err.println(attrValueIdList);
        return baseAttrInfoMapper.selectAttrInfoListByBaseAttrValueIds(attrValueIdList);
    }
}
