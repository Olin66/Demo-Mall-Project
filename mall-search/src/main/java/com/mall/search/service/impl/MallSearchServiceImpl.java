package com.mall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.mall.common.to.es.SkuEsModel;
import com.mall.search.service.MallSearchService;
import com.mall.search.vo.SearchParamVo;
import com.mall.search.vo.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private ElasticsearchClient client;

    @Override
    public SearchResponseVo search(SearchParamVo param) throws IOException {
        SearchResponse<SkuEsModel> response = client.search(s -> s.index("product").query(q -> q.matchAll(m -> m)), SkuEsModel.class);
        return null;
    }
}
