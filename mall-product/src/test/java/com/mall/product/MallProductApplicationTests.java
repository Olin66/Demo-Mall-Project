package com.mall.product;

import com.mall.product.entity.BrandEntity;
import com.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void test(){
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("Huawei");
        brandService.save(brandEntity);
    }

}
