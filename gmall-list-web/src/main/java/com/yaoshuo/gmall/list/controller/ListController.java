package com.yaoshuo.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.bean.es.SkuLsInfo;
import com.yaoshuo.gmall.bean.es.SkuLsParams;
import com.yaoshuo.gmall.bean.es.SkuLsResult;
import com.yaoshuo.gmall.bean.manage.BaseAttrInfo;
import com.yaoshuo.gmall.bean.manage.BaseAttrValue;
import com.yaoshuo.gmall.service.manage.BaseManageService;
import com.yaoshuo.gmall.service.manage.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private BaseManageService baseManageService;

    /**
     * 从es中获取数据，并在页面中展示
     * @param skuLsParams
     * @return
     */
    @RequestMapping("/list.html")
    public String list(SkuLsParams skuLsParams, Model model){

        //设置分页信息
        skuLsParams.setPageSize(2);

        //在es中查询商品信息列表
        SkuLsResult skuLsResult = listService.search(skuLsParams);

        //获取商品信息列表
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        //获取商品对应的平台属性和属性值
        List<BaseAttrInfo> attrInfoList = new ArrayList<BaseAttrInfo>();

        if (!StringUtils.isEmpty(skuLsParams.getCatalog3Id())){
            attrInfoList = baseManageService.getAttrInfoList(skuLsParams.getCatalog3Id());
        }else {
            List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
            attrInfoList = baseManageService.getAttrInfoListByBaseAttrValueIds(attrValueIdList);
        }

        //制作一个最新的访问链接路径
        String urlParam = makeUrlParam(skuLsParams);
        System.err.println(urlParam);

        //创建一个面包屑列表
        List<BaseAttrValue> baseAttrValueList = new ArrayList<BaseAttrValue>();

        //点击对应的属性值后，在平台属性列表中不再显示
        Iterator<BaseAttrInfo> iterator = attrInfoList.iterator();
        while (iterator.hasNext()){

            BaseAttrInfo baseAttrInfo = iterator.next();
            if (baseAttrInfo != null){

                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                if (attrValueList != null && attrValueList.size() > 0){

                    for (BaseAttrValue baseAttrValue : attrValueList) {

                        String[] valueIds = skuLsParams.getValueId();
                        if (valueIds != null && valueIds.length > 0){

                            for (String valueId : valueIds) {
                                if (valueId.equals(baseAttrValue.getId())){
                                    iterator.remove();

                                    //制作面包屑列表
                                    BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                                    baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName() + ": " + baseAttrValue.getValueName());
                                    baseAttrValueSelected.setUrlParam(makeUrlParam(skuLsParams,valueId));

                                    baseAttrValueList.add(baseAttrValueSelected);
                                }
                            }

                        }

                    }
                }
            }
        }

        //保存一个keyword到request作用域中
        String keyword = skuLsParams.getKeyword();

        //获取分页信息
        long totalPages = skuLsResult.getTotalPages();
        int pageNo = skuLsParams.getPageNo();

        //根据浏览次数进行排序




        //展示在es中查询到的商品信息
        model.addAttribute("skuLsInfoList", skuLsInfoList);

        //展示商品的平台属性和平台属性值
        model.addAttribute("attrInfoList", attrInfoList);

        //将最新的访问参数路径放到reques作用域中
        model.addAttribute("urlParam", urlParam);

        //保存一个keyword到request作用域中
        model.addAttribute("keyword", keyword);

        //在request作用域中保存一个面包屑列表
        model.addAttribute("baseAttrValueList", baseAttrValueList);

        //在request作用域中保存分页信息
        model.addAttribute("pageNo", pageNo);
        model.addAttribute("totalPages", totalPages);


        return "list";
    }

    /**
     * 根据用户筛筛选参数拼接最新的访问参数路径
     * @param skuLsParams
     * @return
     */
    private String makeUrlParam(SkuLsParams skuLsParams, String ... excludeValueIds) {

        StringBuilder urlParam = new StringBuilder();

        if (!StringUtils.isEmpty(skuLsParams.getCatalog3Id())){
            urlParam.append("catalog3Id=").append(skuLsParams.getCatalog3Id());
        }

        if (!StringUtils.isEmpty(skuLsParams.getKeyword())){
            if (!StringUtils.isEmpty(skuLsParams.getCatalog3Id())){
                urlParam.append("&");
            }
            urlParam.append("keyword=").append(skuLsParams.getKeyword());
        }

        String[] valueIds = skuLsParams.getValueId();
        if (valueIds != null && valueIds.length > 0){
            for (String valueId : valueIds) {

                if (excludeValueIds != null && excludeValueIds.length > 0){

                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        continue;
                    }
                }

                if (!StringUtils.isEmpty(skuLsParams.getKeyword())){
                    urlParam.append("&");
                }
                urlParam.append("valueId=").append(valueId);
            }
        }

        return urlParam.toString();
    }

}
