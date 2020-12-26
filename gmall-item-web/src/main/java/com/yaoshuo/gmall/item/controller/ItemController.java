package com.yaoshuo.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.annotation.LoginCheckAnnotation;
import com.yaoshuo.gmall.bean.manage.SkuInfo;
import com.yaoshuo.gmall.bean.manage.SpuSaleAttr;
import com.yaoshuo.gmall.service.manage.ListService;
import com.yaoshuo.gmall.service.manage.SkuManageService;
import com.yaoshuo.gmall.service.manage.SpuManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class ItemController {

    @Reference
    private SkuManageService skuManageService;

    @Reference
    private SpuManageService spuManageService;

    @Reference
    private ListService listService;

    /**
     * 根据商品skuId获取商品详情，并在页面中展示
     * @param skuId
     * @param model
     * @return
     */
    @LoginCheckAnnotation(isVerify = false)
    @RequestMapping("/{skuId}.html")
    public String toItem(@PathVariable String skuId, Model model){



        //根据skuId查询商品详情skuInfo，并在页面中展示
        SkuInfo skuInfo = skuManageService.getSkuInfoBySkuId(skuId);

        //根据skuId更新商品的热度评分
        listService.updateSkuLsHotScoreBySkuId(skuId);

        //获取商品销售属性列表
        List<SpuSaleAttr> spuSaleAttrList =  spuManageService.getSpuSaleAttrListBySpuIdAndSkuId(skuInfo.getSpuId(),skuId);

        //点击商品销售属性，切换不同的skuId，若不存在该skuId则不切换映射路径
        String skuSaleAttrMapStr = skuManageService.getSkuSaleAttrValueListBySpuId(skuInfo.getSpuId());

        model.addAttribute("skuSaleAttrMapStr",skuSaleAttrMapStr);

        model.addAttribute("skuInfo",skuInfo);
        model.addAttribute("spuSaleAttrList",spuSaleAttrList);
        model.addAttribute("originUrl","http://item.gmall.com/" + skuId + ".html");

        return "item";
    }
}
