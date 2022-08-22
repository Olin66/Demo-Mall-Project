package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.AttrGroupEntity;
import com.mall.product.vo.AttrGroupWithAttrsVo;
import com.mall.product.vo.pojo.SpuAttrGroup;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 17:40:24
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    List<SpuAttrGroup> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

