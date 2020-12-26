package com.yaoshuo.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yaoshuo.gmall.bean.manage.*;
import com.yaoshuo.gmall.manage.mapper.*;
import com.yaoshuo.gmall.service.manage.SpuManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpuManageServiceImpl implements SpuManageService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;


    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        if (spuInfo != null) {
            spuInfoMapper.insertSelective(spuInfo);

            List<SpuImage> spuImageList = spuInfo.getSpuImageList();
            if (spuImageList != null && spuImageList.size() > 0) {
                for (SpuImage spuImage : spuImageList) {
                    spuImage.setSpuId(spuInfo.getId());
                    spuImageMapper.insertSelective(spuImage);
                }
            }

            List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
            if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
                for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                    spuSaleAttr.setSpuId(spuInfo.getId());
                    spuSaleAttrMapper.insertSelective(spuSaleAttr);


                    List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                    if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                        for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                            spuSaleAttrValue.setSpuId(spuInfo.getId());
                            spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                        }
                    }

                }
            }

        }

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuIdAndSkuId(String spuId, String skuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListBySpuIdAndSkuId(spuId,skuId);
    }

}
