package com.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mall.product.entity.BrandEntity;
import com.mall.product.vo.BrandVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mall.product.entity.CategoryBrandRelationEntity;
import com.mall.product.service.CategoryBrandRelationService;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;


/**
 * 品牌分类关联
 *
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 17:40:24
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @GetMapping("/brands/list")
    public R relationBrandList(@RequestParam(value="catId") Long catId){
        List<BrandEntity> entities = categoryBrandRelationService.getBrandsByCatId(catId);
        List<BrandVo> brandVos = entities.stream().map(entity -> {
            BrandVo vo = new BrandVo();
            vo.setBrandId(entity.getBrandId());
            vo.setBrandName(entity.getName());
            return vo;
        }).toList();
        return R.ok().put("data", brandVos);
    }

    @GetMapping("/catelog/list")
    public R catelogList(@RequestParam("brandId") Long brandId) {
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService
                .list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
