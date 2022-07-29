package com.mall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.mall.common.to.es.SkuEsModel;
import com.mall.search.constant.EsConstant;
import com.mall.search.service.ProductSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    private ElasticsearchClient client;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        List<BulkOperation> bulkOperations = new ArrayList<>();
        for (SkuEsModel skuEsModel : skuEsModels) {
            bulkOperations
                    .add(new BulkOperation.Builder()
                            .create(d -> d.document(skuEsModel)
                                    .id(String.valueOf(skuEsModel.getSkuId()))
                                    .index(EsConstant.PRODUCT_INDEX)).build());
        }
        BulkResponse response = client.bulk(e->e.index(EsConstant.PRODUCT_INDEX).operations(bulkOperations));
        return response.errors();
    }
}
