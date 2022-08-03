package com.mall.search.controller;

import com.mall.search.service.MallSearchService;
import com.mall.search.vo.SearchParamVo;
import com.mall.search.vo.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParamVo param){
        SearchResponseVo response = mallSearchService.search(param);
        return "list";
    }
}
