package com.mall.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.member.entity.MemberLevelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 19:31:00
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();
}
