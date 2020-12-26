package com.yaoshuo.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.bean.es.SkuLsInfo;
import com.yaoshuo.gmall.bean.manage.SkuInfo;
import com.yaoshuo.gmall.service.manage.ListService;
import com.yaoshuo.gmall.service.manage.SkuManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private SkuManageService skuManageService;

    @Reference
    private ListService listService;

    /**
     * 保存商品skuInfo信息到数据库
     * //http://localhost:8082/saveSkuInfo
     * @param skuInfo
     */
    @RequestMapping("/saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuManageService.saveSkuInfo(skuInfo);
    }

    /**
     * 根据skuId保存商品信息到es中====商品上架
     * @param skuId
     */
    @RequestMapping("/onSale")
    public void onSale(String skuId){

        SkuInfo skuInfo = skuManageService.getSkuInfoBySkuId(skuId);

        SkuLsInfo skuLsInfo = new SkuLsInfo();

        BeanUtils.copyProperties(skuInfo,skuLsInfo);

        listService.saveSkuLsInfo(skuLsInfo);

    }

}
