package com.yaoshuo.gmall.bean.manage;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

@Data
public class SpuSaleAttr implements Serializable {

    @Id
    @Column
    private String id;

    @Column
    private String spuId;

    @Column
    private String saleAttrId;

    @Column
    private String saleAttrName;

    @Transient
    private List<SpuSaleAttrValue> spuSaleAttrValueList;

}
