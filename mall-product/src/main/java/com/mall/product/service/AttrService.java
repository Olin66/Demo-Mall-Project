package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.AttrEntity;
import com.mall.product.vo.AttrGroupRelationVo;
import com.mall.product.vo.AttrRespVo;
import com.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 17:40:24
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(List<AttrGroupRelationVo> vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);
}

