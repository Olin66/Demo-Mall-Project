package com.mall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.NoStockException;
import com.mall.common.to.SkuHasStockTo;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.ware.dao.WareSkuDao;
import com.mall.ware.entity.WareSkuEntity;
import com.mall.ware.feign.ProductFeignService;
import com.mall.ware.service.WareSkuService;
import com.mall.ware.vo.SkuWareHasStockVo;
import com.mall.ware.vo.WareItemVo;
import com.mall.ware.vo.WareSkuLockVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void addStack(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.isEmpty()) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    String json = JSON.toJSONString(info.get("data"));
                    JSONObject object = JSONObject.parseObject(json);
                    Map<String, Object> data = new HashMap<>(object);
                    String skuName = (String) data.get("skuName");
                    wareSkuEntity.setSkuName(skuName);
                }
            } catch (Exception e) {
                log.error("{}", e);
            }
            this.baseMapper.insert(wareSkuEntity);
        } else this.baseMapper.addStack(skuId, wareId, skuNum);
    }

    @Override
    public List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds) {
        return skuIds.stream().map(sku -> {
            SkuHasStockTo vo = new SkuHasStockTo();
            Long count = baseMapper.getSkuStock(sku);
            vo.setSkuId(sku);
            vo.setHasStock(count != null && count > 0);
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public void orderLockStock(WareSkuLockVo vo) {
        List<WareItemVo> locks = vo.getLocks();
        List<SkuWareHasStockVo> hasStocks = locks.stream().map(item -> {
            SkuWareHasStockVo stock = new SkuWareHasStockVo();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = this.baseMapper.listWareIdHasSkuStock(skuId);
            stock.setWareIds(wareIds);
            return stock;
        }).toList();
        for (SkuWareHasStockVo hasStock : hasStocks) {
            boolean lock = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            if (wareIds == null || wareIds.isEmpty()) {
                throw new NoStockException();
            }
            for (Long wareId : wareIds) {
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    lock = true;
                    break;
                }
            }
            if (!lock) {
                throw new NoStockException();
            }
        }
    }

}