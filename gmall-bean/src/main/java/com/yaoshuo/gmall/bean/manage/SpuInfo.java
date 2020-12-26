package com.yaoshuo.gmall.bean.manage;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class SpuInfo implements Serializable {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String spuName;

    @Column
    private String description;

    @Column
    private String catalog3Id;

    //商品销售属性列表
    @Transient
    private List<SpuSaleAttr> spuSaleAttrList;

    //商品图片列表
    @Transient
    private List<SpuImage> spuImageList;


}
