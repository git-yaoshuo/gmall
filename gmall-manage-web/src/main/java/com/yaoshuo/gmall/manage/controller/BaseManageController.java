package com.yaoshuo.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.bean.manage.*;
import com.yaoshuo.gmall.service.manage.BaseManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class BaseManageController {

    @Reference
    private BaseManageService baseManageService;



    /**
     * 获取商品一级分类列表
     * //http://localhost:8082/getCatalog1
     * @return
     */
    @RequestMapping("/getCatalog1")
    public List<BaseCatalog1> getCatalog1() {
        return baseManageService.getCatalog1();
    }



    /**
     * 根据商品一级分类catalog1Id获取商品二级分类列表
     * //http://localhost:8082/getCatalog2?catalog1Id=2
     * @param catalog1Id
     * @return
     */
    @RequestMapping("/getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        return baseManageService.getCatalog2(catalog1Id);
    }



    /**
     * 根据商品二级分类catalog2Id获取商品三级分类列表
     * //http://localhost:8082/getCatalog3?catalog2Id=14
     * @param catalog2Id
     * @return
     */
    @RequestMapping("/getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        return baseManageService.getCatalog3(catalog2Id);
    }



    /**
     * 根据商品三级分类catalog3Id获取商品基本平台属性列表
     * //http://localhost:8082/attrInfoList?catalog3Id=64
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/attrInfoList")
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
        return baseManageService.getAttrInfoList(catalog3Id);
    }



    /**
     * 保存商品基本平台属性到数据库
     * //http://localhost:8082/saveAttrInfo
     * @param baseAttrInfo
     */
    @RequestMapping("/saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        baseManageService.saveAttrInfo(baseAttrInfo);
    }



    /**
     * 根据商品基本属性attrId获取商品基本平台属性值列表
     * //http://localhost:8082/getAttrValueList?attrId=23
     * @param attrId
     * @return
     */
    @RequestMapping("/getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrInfo baseAttrInfo = baseManageService.getAttrValueList(attrId);
        return baseAttrInfo.getAttrValueList();
    }

}
