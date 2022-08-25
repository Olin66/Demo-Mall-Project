package com.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.member.dao.MemberDao;
import com.mall.member.dao.MemberLevelDao;
import com.mall.member.entity.MemberEntity;
import com.mall.member.entity.MemberLevelEntity;
import com.mall.member.exception.PhoneExistedException;
import com.mall.member.exception.UsernameExistedException;
import com.mall.member.service.MemberService;
import com.mall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {
        MemberEntity member = new MemberEntity();
        MemberLevelEntity level = memberLevelDao.getDefaultLevel();
        member.setLevelId(level.getId());
        checkUsernameUnique(vo.getUsername());
        checkPhoneUnique(vo.getPhone());
        member.setUsername(vo.getUsername());
        member.setMobile(vo.getPhone());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        member.setPassword(encode);
        this.baseMapper.insert(member);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistedException {
        Long count = this.baseMapper
                .selectCount(new QueryWrapper<MemberEntity>()
                        .eq("mobile", phone));
        if (count > 0) throw new PhoneExistedException();
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistedException {
        Long count = this.baseMapper
                .selectCount(new QueryWrapper<MemberEntity>()
                        .eq("username", username));
        if (count > 0) throw new UsernameExistedException();
    }
}