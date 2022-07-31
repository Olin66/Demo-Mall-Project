package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.dao.CategoryDao;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryBrandRelationService;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.CatalogSecondVo;
import com.mall.product.vo.CatalogThirdVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        return entities.stream()
                .filter(o -> o.getParentCid() == 0)
                .peek(menu -> menu.setChildren(getChildren(menu, entities)))
                .sorted(Comparator.comparingInt(m -> (m.getSort() == null ? 0 : m.getSort())))
                .toList();
    }

    @Override
    public Long[] getCatelogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        getParentPath(path, catelogId);
        return path.toArray(new Long[0]);
    }

    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Override
    public List<CategoryEntity> getLevelOneCategories() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    public Map<String, List<CatalogSecondVo>> getCatalogJson() {
        List<CategoryEntity> levelOne = getLevelOneCategories();
        Map<String, List<CatalogSecondVo>> collect = levelOne.stream().collect(Collectors
                .toMap(k -> k.getCatId().toString(), v -> {
                    List<CategoryEntity> entities2 = baseMapper.selectList(new QueryWrapper<CategoryEntity>().
                            eq("parent_cid", v.getCatId()));
                    List<CatalogSecondVo> catalogSecondVos = null;
                    if (entities2 != null) {
                        catalogSecondVos = entities2.stream().map(item -> {
                            List<CategoryEntity> entities3 = baseMapper.selectList(new QueryWrapper<CategoryEntity>()
                                    .eq("parent_cid", item.getCatId()));
                            List<CatalogThirdVo> thirds = null;
                            if (entities3 != null) {
                                thirds = entities3.stream().map(l ->
                                        new CatalogThirdVo(item.getCatId().toString(),
                                                l.getCatId().toString(), l.getName())).toList();
                            }
                            return new CatalogSecondVo(v.getCatId().toString(), thirds,
                                    item.getCatId().toString(), item.getName());
                        }).toList();
                    }
                    return catalogSecondVos;
                }));
        return collect;
    }

    private void getParentPath(List<Long> list, Long catelogId) {
        CategoryEntity category = this.getById(catelogId);
        if (category.getParentCid() != 0) {
            getParentPath(list, category.getParentCid());
        }
        list.add(catelogId);
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(entity -> Objects.equals(root.getCatId(), entity.getParentCid()))
                .peek(entity -> entity.setChildren(getChildren(entity, all)))
                .sorted(Comparator.comparingInt(m -> (m.getSort() == null ? 0 : m.getSort())))
                .toList();
    }

}