package com.mall.search.controller;

import com.mall.common.exception.ExceptionCodeEnum;
import com.mall.common.to.es.SkuEsModel;
import com.mall.common.utils.R;
import com.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search/save")
public class EsSaveController {
    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b;
        try {
            b = productSaveService.productStatusUp(skuEsModels);

        } catch (Exception e) {
            log.error("ElasticSaveController商品上架错误！", e);
            return R.error(ExceptionCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),
                    ExceptionCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (!b) return R.ok();
        else return R.error(ExceptionCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),
                ExceptionCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
    }
}
