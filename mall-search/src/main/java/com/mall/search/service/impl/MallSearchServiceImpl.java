package com.mall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.mall.common.to.es.SkuEsModel;
import com.mall.search.constant.EsConstant;
import com.mall.search.service.MallSearchService;
import com.mall.search.vo.SearchParamVo;
import com.mall.search.vo.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
            sb.highlight(h -> h.fields("skuTitle", f -> f.preTags("<b style='color:red'>").postTags("</b>")));
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
        SearchRequest request = sb.build();
        SearchResponse<SkuEsModel> response = client.search(request, SkuEsModel.class);
        for (Hit<SkuEsModel> hit : response.hits().hits()) System.out.println(hit.source());
        return null;
    }
}
