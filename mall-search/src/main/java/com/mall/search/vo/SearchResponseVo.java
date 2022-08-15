package com.mall.search.vo;

import com.mall.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {
    private List<SkuEsModel> products;
    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<Integer> pageNavs;
    private List<BrandVo> brands;
    private List<AttrVo> attrs;
    private List<CatalogVo> catalogs;
}
