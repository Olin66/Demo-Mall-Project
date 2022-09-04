package com.mall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.ware.dao.WareInfoDao;
import com.mall.ware.entity.WareInfoEntity;
import com.mall.ware.feign.MemberFeignService;
import com.mall.ware.service.WareInfoService;
import com.mall.ware.vo.FareVo;
import com.mall.ware.vo.WareAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.addrInfo(addrId);
        String json = JSON.toJSONString(r.get("memberReceiveAddress"));
        WareAddressVo vo = JSONObject.parseObject(json, WareAddressVo.class);
        if (vo != null) {
            String phone = vo.getPhone();
            String fare = phone.substring(phone.length() - 1);
            fareVo.setFare(new BigDecimal(fare));
            fareVo.setAddress(vo);
        }
        return fareVo;
    }

}