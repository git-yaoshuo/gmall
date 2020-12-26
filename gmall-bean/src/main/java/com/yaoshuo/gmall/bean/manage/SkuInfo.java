package com.yaoshuo.gmall.bean.manage;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SkuInfo implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String spuId;

    @Column
    private BigDecimal price;

    @Column
    private String skuName;

    @Column
    private String skuDesc;

    @Column
    private BigDecimal weight;

    @Column
    private String tmId;    //品牌id

    @Column
    private String catalog3Id;

    @Column
    private String skuDefaultImg;

    @Transient
    private List<SkuAttrValue> skuAttrValueList = new ArrayList<SkuAttrValue>();

    @Transient
    private List<SkuImage> skuImageList = new ArrayList<SkuImage>();

    @Transient
    private List<SkuSaleAttrValue> skuSaleAttrValueList = new ArrayList<SkuSaleAttrValue>();

}
