package com.yaoshuo.gmall.bean.es;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SkuLsResult implements Serializable {

    private List<SkuLsInfo> skuLsInfoList = new ArrayList<SkuLsInfo>();

    private long total;

    private long totalPages;

    private List<String> attrValueIdList = new ArrayList<String>();

}
