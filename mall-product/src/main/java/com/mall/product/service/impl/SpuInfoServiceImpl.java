package com.mall.product.service.impl;

import com.mall.common.to.SkuReductionTo;
import com.mall.common.to.SpuBoundsTo;
import com.mall.common.utils.R;
import com.mall.product.entity.*;
import com.mall.product.feign.CouponFeignService;
import com.mall.product.service.*;
import com.mall.product.vo.SpuSaveVo;
import com.mall.product.vo.pojo.*;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );
        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo spuInfoVo) {
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        List<String> list = spuInfoVo.getDecript();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", list));
        spuInfoDescService.saveSpuDescript(spuInfoDescEntity);

        List<String> images = spuInfoVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        List<BaseAttrs> baseAttrs = spuInfoVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity value = new ProductAttrValueEntity();
            value.setAttrId(attr.getAttrId());
            AttrEntity byId = attrService.getById(attr.getAttrId());
            value.setAttrName(byId.getAttrName());
            value.setAttrValue(attr.getAttrValues());
            value.setQuickShow(attr.getShowDesc());
            value.setSpuId(spuInfoEntity.getId());
            return value;
        }).toList();
        productAttrValueService.saveProductAttr(productAttrValueEntities);

        Bounds bounds = spuInfoVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        R r1 = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (r1.getCode() != 0) log.error("远程保存spu积分信息失败！");

        List<Skus> skus = spuInfoVo.getSkus();
        if (skus != null && !skus.isEmpty()) {
            skus.forEach(sku -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSaleCount(0L);
                for (Images img : sku.getImages()) {
                    if (img.getDefaultImg() == 1) {
                        skuInfoEntity.setSkuDefaultImg(img.getImgUrl());
                    }
                }
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(img -> !StringUtils.isEmpty(img.getImgUrl())).toList();
                skuImagesService.saveBatch(skuImagesEntities);
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).toList();
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                if (sku.getFullCount() > 0 || sku.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
                    SkuReductionTo skuReductionTo = new SkuReductionTo();
                    BeanUtils.copyProperties(sku, skuReductionTo);
                    skuReductionTo.setSkuId(skuId);
                    R r2 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r2.getCode() != 0) log.error("远程保存sku优惠信息失败！");
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

}