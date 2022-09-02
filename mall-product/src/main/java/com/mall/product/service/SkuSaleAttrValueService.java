package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.SkuSaleAttrValueEntity;
import com.mall.product.vo.pojo.SkuSaleAttr;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 17:40:24
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuSaleAttr> getSaleAttrsBySpuId(Long spuId);

    List<String> getSkuSakeAttrValuesAsStringList(Long skuId);
}

