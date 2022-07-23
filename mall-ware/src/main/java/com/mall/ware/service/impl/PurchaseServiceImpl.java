package com.mall.ware.service.impl;

import com.mall.common.constant.WareConstant;
import com.mall.ware.entity.PurchaseDetailEntity;
import com.mall.ware.service.PurchaseDetailService;
import com.mall.ware.service.WareSkuService;
import com.mall.ware.vo.MergeVo;
import com.mall.ware.vo.PurchaseFinishVo;
import com.mall.ware.vo.PurchaseItemFinishVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.ware.dao.PurchaseDao;
import com.mall.ware.entity.PurchaseEntity;
import com.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0).or().eq("status", 1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
        }
        List<Long> items = mergeVo.getItems();
        List<PurchaseDetailEntity> entities = purchaseDetailService.listByIds(items);

        entities.forEach((item) -> {
            if (!item.getStatus().equals(WareConstant.PurchaseDetailEnum.CREATED.getCode())
                    && !item.getStatus().equals(WareConstant.PurchaseDetailEnum.ASSIGNED.getCode())) {
                throw new IllegalArgumentException("订单已经被领取！");
            }
        });
        List<PurchaseDetailEntity> list = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(purchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).toList();
        purchaseDetailService.updateBatchById(list);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    @Transactional
    public void receive(List<Long> ids) {
        List<PurchaseEntity> list = ids.stream().map(this::getById).filter(item ->
                        item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                                item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .peek(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
                    item.setUpdateTime(new Date());
                })
                .toList();
        this.updateBatchById(list);
        list.forEach(item -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            entities.forEach(entity -> entity.setStatus(WareConstant.PurchaseDetailEnum.PURCHASING.getCode()));
            purchaseDetailService.updateBatchById(entities);
        });
    }

    @Override
    @Transactional
    public void finish(PurchaseFinishVo purchaseFinishVo) {
        Long id = purchaseFinishVo.getId();
        boolean flag = true;
        List<PurchaseItemFinishVo> items = purchaseFinishVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemFinishVo item : items) {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.ERROR.getCode()) {
                flag = false;
                entity.setStatus(item.getStatus());
            } else {
                entity.setStatus(WareConstant.PurchaseDetailEnum.FINISHED.getCode());
                PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStack(byId.getSkuId(), byId.getWareId(), byId.getSkuNum());
            }
            entity.setId(item.getItemId());
            updates.add(entity);
        }
        purchaseDetailService.updateBatchById(updates);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() :
                WareConstant.PurchaseStatusEnum.ERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}