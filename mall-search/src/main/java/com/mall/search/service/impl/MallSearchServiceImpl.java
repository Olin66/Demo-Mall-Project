package com.mall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;
import com.mall.common.to.es.SkuEsModel;
import com.mall.search.constant.EsConstant;
import com.mall.search.service.MallSearchService;
import com.mall.search.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private ElasticsearchClient client;

    @Override
    public SearchResponseVo search(SearchParamVo param) throws IOException {
        SearchRequest.Builder sb = new SearchRequest.Builder();
        sb.index(EsConstant.PRODUCT_INDEX);
        Query.Builder qb = new Query.Builder();
        BoolQuery.Builder bb = new BoolQuery.Builder();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            bb.must(m -> m.match(item -> item.field("skuTitle").query(param.getKeyword())));
            sb.highlight(h -> h.fields("skuTitle", f -> f.preTags("<b style='color:blue'>").postTags("</b>")));
        }
        if (param.getCatalog3Id() != null) {
            bb.filter(f -> f.term(t -> t.field("catalogId").value(param.getCatalog3Id())));
        }
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            List<FieldValue> fieldValues = param.getBrandId().stream().map(FieldValue::of).toList();
            bb.filter(f -> f.terms(t -> t.field("brandId").terms(v -> v.value(fieldValues))));
        }
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            for (String attrStr : param.getAttrs()) {
                String[] strings = attrStr.split("_");
                String attrId = strings[0];
                String[] attrValues = strings[1].split(":");
                List<FieldValue> fieldValues = Arrays.stream(attrValues).map(FieldValue::of).toList();
                bb.filter(f -> f.nested(n -> n.path("attrs").query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("attrs.attrId").value(attrId)))
                        .must(m -> m.terms(t -> t.field("attrs.attrValue").terms(v -> v.value(fieldValues))))
                ))));
            }
        }
        if (param.getHasStock() != null) {
            bb.filter(f -> f.term(t -> t.field("hasStock").value(param.getHasStock() == 1)));
        }
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            String[] strings = param.getSkuPrice().split("_");
            bb.filter(f -> f.range(r -> r.field("skuPrice")
                    .gte(JsonData.of(strings[0])).lte(JsonData.of(strings[1]))));
        }
        sb.query(qb.bool(bb.build()).build());
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] strings = param.getSort().split("_");
            SortOrder so = strings[1].equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc;
            sb.sort(ss -> ss.field(f -> f.field(strings[0]).order(so)));
        }
        sb.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        sb.size(EsConstant.PRODUCT_PAGE_SIZE);
        Map<String, Aggregation> aggMap1 = new HashMap<>();
        Aggregation.Builder ab1 = new Aggregation.Builder();
        Aggregation.Builder ab2 = new Aggregation.Builder();
        Aggregation.Builder ab3 = new Aggregation.Builder();
        Aggregation.Builder ab4 = new Aggregation.Builder();
        Aggregation.Builder ab5 = new Aggregation.Builder();
        Aggregation.Builder ab6 = new Aggregation.Builder();
        Aggregation.Builder ab7 = new Aggregation.Builder();
        Aggregation.Builder ab8 = new Aggregation.Builder();
        Aggregation.Builder ab9 = new Aggregation.Builder();
        Map<String, Aggregation> aggMap2 = new HashMap<>();
        Aggregation a1 = ab1.terms(t -> t.field("brandName").size(100)).build();
        Aggregation a2 = ab2.terms(t -> t.field("brandImg").size(100)).build();
        aggMap2.put("brand_name_agg", a1);
        aggMap2.put("brand_img_agg", a2);
        Map<String, Aggregation> aggMap3 = new HashMap<>();
        Aggregation a5 = ab5.terms(t -> t.field("catalogName").size(100)).build();
        aggMap3.put("catalog_name_agg", a5);
        Aggregation a3 = ab3.terms(t -> t.field("brandId").size(100)).aggregations(aggMap2).build();
        Aggregation a4 = ab4.terms(t -> t.field("catalogId").size(100)).aggregations(aggMap3).build();
        Map<String, Aggregation> aggMap4 = new HashMap<>();
        Aggregation a8 = ab8.terms(t -> t.field("attrs.attrName").size(100)).build();
        Aggregation a9 = ab9.terms(t -> t.field("attrs.attrValue").size(100)).build();
        aggMap4.put("attr_name_agg", a8);
        aggMap4.put("attr_value_agg", a9);
        Aggregation a7 = ab7.terms(t -> t.field("attrs.attrId").size(100)).aggregations(aggMap4).build();
        Map<String, Aggregation> aggMap5 = new HashMap<>();
        aggMap5.put("attr_id_agg", a7);
        Aggregation a6 = ab6.nested(v -> v.path("attrs")).aggregations(aggMap5).build();
        aggMap1.put("brand_agg", a3);
        aggMap1.put("catalog_agg", a4);
        aggMap1.put("attr_agg", a6);
        sb.aggregations(aggMap1);
        SearchRequest request = sb.build();
        SearchResponse<SkuEsModel> response = client.search(request, SkuEsModel.class);
        return buildSearchResponseVo(response, param);
    }

    private SearchResponseVo buildSearchResponseVo(SearchResponse<SkuEsModel> response, SearchParamVo param) {
        SearchResponseVo vo = new SearchResponseVo();
        HitsMetadata<SkuEsModel> hits = response.hits();
        TotalHits total = hits.total();
        Long value = Objects.requireNonNull(total).value();
        int totalPages = (int) (value % EsConstant.PRODUCT_PAGE_SIZE == 0 ?
                (value / EsConstant.PRODUCT_PAGE_SIZE) : (value / EsConstant.PRODUCT_PAGE_SIZE + 1));
        List<Hit<SkuEsModel>> list = hits.hits();
        if (list != null && !list.isEmpty()) {
            List<SkuEsModel> skus = list.stream().map(hit -> {
                SkuEsModel source = hit.source();
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    String skuTitle = hit.highlight().get("skuTitle").get(0);
                    Objects.requireNonNull(source).setSkuTitle(skuTitle);
                }
                if (source == null) return new SkuEsModel();
                return source;
            }).toList();
            vo.setProducts(skus);
        }
        vo.setTotal(value);
        vo.setTotalPages(totalPages);
        vo.setPageNum(param.getPageNum());
        List<LongTermsBucket> catalogAgg = response.aggregations().get("catalog_agg").lterms().buckets().array();
        List<CatalogVo> catalogVos = catalogAgg.stream().map(bucket -> {
            CatalogVo catalogVo = new CatalogVo();
            Long key = bucket.key();
            catalogVo.setCatalogId(key);
            String catalogName = bucket.aggregations().get("catalog_name_agg").sterms().buckets().array().get(0).key();
            catalogVo.setCatalogName(catalogName);
            return catalogVo;
        }).toList();
        vo.setCatalogs(catalogVos);
        List<LongTermsBucket> brandAgg = response.aggregations().get("brand_agg").lterms().buckets().array();
        List<BrandVo> brandVos = brandAgg.stream().map(bucket -> {
            BrandVo brandVo = new BrandVo();
            long key = bucket.key();
            brandVo.setBrandId(key);
            String brandImg = bucket.aggregations().get("brand_img_agg").sterms().buckets().array().get(0).key();
            brandVo.setBrandImg(brandImg);
            String brandName = bucket.aggregations().get("brand_name_agg").sterms().buckets().array().get(0).key();
            brandVo.setBrandName(brandName);
            return brandVo;
        }).toList();
        vo.setBrands(brandVos);
        NestedAggregate attrAgg = response.aggregations().get("attr_agg").nested();
        List<LongTermsBucket> attrIdAgg = attrAgg.aggregations().get("attr_id_agg").lterms().buckets().array();
        List<AttrVo> attrVos = attrIdAgg.stream().map(bucket -> {
            AttrVo attrVo = new AttrVo();
            long key = bucket.key();
            attrVo.setAttrId(key);
            String attrName = bucket.aggregations().get("attr_name_agg").sterms().buckets().array().get(0).key();
            attrVo.setAttrName(attrName);
            List<String> strings
                    = bucket.aggregations().get("attr_value_agg")
                    .sterms().buckets().array().stream().map(StringTermsBucket::key)
                    .toList();
            attrVo.setAttrValue(strings);
            return attrVo;
        }).toList();
        vo.setAttrs(attrVos);
        return vo;
    }
}
