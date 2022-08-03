package com.mall.search.service;

import com.mall.search.vo.SearchParamVo;
import com.mall.search.vo.SearchResponseVo;

public interface MallSearchService {
    SearchResponseVo search(SearchParamVo param);
}
