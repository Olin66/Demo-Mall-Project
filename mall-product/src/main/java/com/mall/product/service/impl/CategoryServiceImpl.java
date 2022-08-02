package com.mall.product.service.impl;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.dao.CategoryDao;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryBrandRelationService;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.CatalogSecondVo;
import com.mall.product.vo.CatalogThirdVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;


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
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String catalogJSON = ops.get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            return getCatalogJsonFromDatabaseWithRedisLock();
        }
        return JacksonUtils.toObj(catalogJSON, new TypeReference<>() {
        });
    }

    public Map<String, List<CatalogSecondVo>> getCatalogJsonFromDatabaseWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue()
                .setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        while (true) {
            if (Boolean.TRUE.equals(lock)) {
                System.out.println("Locked!");
                Map<String, List<CatalogSecondVo>> result;
                try {
                    result = getDataFromDatabase();
                } finally {
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    stringRedisTemplate.execute(new DefaultRedisScript<>(script), List.of("lock"), uuid);
                }
                return result;
            }
            lock = stringRedisTemplate.opsForValue()
                    .setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
            System.out.println("Unlocked!");
        }
    }

    public synchronized Map<String, List<CatalogSecondVo>> getCatalogJsonFromDatabaseWithLocalLock() {
        return getDataFromDatabase();
    }

    private Map<String, List<CatalogSecondVo>> getDataFromDatabase() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String catalogJSON = ops.get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            return JacksonUtils.toObj(catalogJSON, new TypeReference<>() {
            });
        }
        List<CategoryEntity> list = baseMapper.selectList(null);
        List<CategoryEntity> levelOne = getParentCid(list, 0L);
        Map<String, List<CatalogSecondVo>> result = levelOne.stream().collect(Collectors
                .toMap(k -> k.getCatId().toString(), v -> {
                    List<CategoryEntity> entities2 = getParentCid(list, v.getParentCid());
                    List<CatalogSecondVo> catalogSecondVos = null;
                    if (entities2 != null) {
                        catalogSecondVos = entities2.stream().map(item -> {
                            List<CategoryEntity> entities3 = getParentCid(list, item.getParentCid());
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
                    if (catalogSecondVos == null) return new ArrayList<>();
                    else return catalogSecondVos;
                }));
        String json = JacksonUtils.toJson(result);
        ops.set("catalogJSON", json, 1, TimeUnit.DAYS);
        return result;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> list, Long parentCid) {
        return list.stream().filter(o -> Objects.equals(o.getParentCid(), parentCid)).toList();
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