package com.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.coupon.dao.SeckillSessionDao;
import com.mall.coupon.entity.SeckillSessionEntity;
import com.mall.coupon.entity.SeckillSkuRelationEntity;
import com.mall.coupon.service.SeckillSessionService;
import com.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatestThreeDaysSession() {
        List<SeckillSessionEntity> temp = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime(), endTime()));
        if (temp != null && !temp.isEmpty()) {
            return temp.stream().peek(item -> {
                Long id = item.getId();
                List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService
                        .list(new QueryWrapper<SeckillSkuRelationEntity>()
                                .eq("promotion_session_id", id));
                item.setRelationSkus(relationEntities);
            }).toList();
        }
        return null;
    }

    private String startTime() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime() {
        LocalDateTime end = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}