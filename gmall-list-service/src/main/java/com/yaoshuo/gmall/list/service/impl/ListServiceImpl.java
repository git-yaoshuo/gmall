package com.yaoshuo.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yaoshuo.gmall.bean.es.SkuLsInfo;
import com.yaoshuo.gmall.bean.es.SkuLsParams;
import com.yaoshuo.gmall.bean.es.SkuLsResult;
import com.yaoshuo.gmall.constant.ESConstant;
import com.yaoshuo.gmall.service.manage.ListService;
import com.yaoshuo.gmall.util.JedisUtils;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    /**
     * 保存skuInfo到es中===商品上架
     * @param skuLsInfo
     */
    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {

        /**
         * 1、创建dsl语句
         * 2、定义执行命令
         * 3、执行语句
         * 4、处理返回结果集
         */

        Index index = new Index.Builder(skuLsInfo).index(ESConstant.ES_INDEX).type(ESConstant.ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            DocumentResult result = jestClient.execute(index);
            System.err.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据用户输入的查询条件，再es中查询数据信息
     * @param skuLsParams
     * @return
     */
    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {

        /*
        1、定义dsl语句
        2、定义执行动作
        3、执行dsl语句
        4、处理返回结果集
         */

        SkuLsResult skuLsResult = null;
        try {
            String query = makeQueryStringForSearch(skuLsParams);

            Search search = new Search.Builder(query).addIndex(ESConstant.ES_INDEX).addType(ESConstant.ES_TYPE).build();

            SearchResult searchResult = jestClient.execute(search);

            skuLsResult = makeResultForSearch(skuLsParams, searchResult);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuLsResult;
    }

    @Override
    public void updateSkuLsHotScoreBySkuId(String skuId) {

         /*
            1、定义dsl语句
            2、定义执行动作
            3、执行dsl语句
         */
        Jedis jedis = JedisUtils.getJedis();
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);

        int countToES = 10;

        if (hotScore % countToES == 0){

            String query = "{\n" +
                    "  \"doc\": {\n" +
                    "    \"hotScore\":1\n" +
                    "  }\n" +
                    "}";

            Update update = new Update.Builder(query).index(ESConstant.ES_INDEX).type(ESConstant.ES_TYPE).id(skuId).build();

            try {
                jestClient.execute(update);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据用户搜索条件手写dsl动态语句
     * @param skuLsParams
     * @return
     */
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {

        //创建一个查询的builder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //1、查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //1、1 过滤 catalo3Id
        if (!StringUtils.isEmpty(skuLsParams.getCatalog3Id())){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //1、2 过滤 skuAttrValueList.valueId
        String[] valueIds = skuLsParams.getValueId();
        if (valueIds != null && valueIds.length > 0){
            for (String valueId : valueIds) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //1.3 匹配关键字skuName
        if (!StringUtils.isEmpty(skuLsParams.getKeyword())){

            //1.3.1 匹配关键字skuName
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

            //1.3.2 设置高亮显示关键字skuName
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            highlightBuilder.field("skuName");

            searchSourceBuilder.highlight(highlightBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);


        //2、分页
        int from = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //3、排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //4、聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        String query = searchSourceBuilder.toString();
        System.err.println("query = " + query);

        return query;
    }

    /**
     *
     * 处理查询es后返回的结果集
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {

        SkuLsResult skuLsResult = new SkuLsResult();

        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();

        //设置skuLsInfo列表
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {

            SkuLsInfo skuLsInfo = hit.source;

            //设置高亮字段
            Map<String, List<String>> highlight = hit.highlight;
            if (highlight != null && highlight.size() > 0){
                String skuName = highlight.get("skuName").get(0);
                skuLsInfo.setSkuName(skuName);
            }
            skuLsInfoList.add(skuLsInfo);
        }

        //设置总记录数
        skuLsResult.setTotal(searchResult.getTotal());

        //设置总页数
        //long totalPages = searchResult.getTotal() % skuLsParams.getPageSize() == 0 ? searchResult.getTotal() / skuLsParams.getPageSize() : (searchResult.getTotal() / skuLsParams.getPageSize()) + 1 ;
        long totalPages = (searchResult.getTotal() + skuLsParams.getPageSize() - 1 ) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);

        //设置平台属性值列表attrValueIdList
        TermsAggregation groupby_attr = searchResult.getAggregations().getTermsAggregation("groupby_attr");

        if (groupby_attr != null){
            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                attrValueIdList.add(bucket.getKey());
            }
        }

        return skuLsResult;
    }

}
