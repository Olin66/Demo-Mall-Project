package com.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.member.entity.MemberEntity;
import com.mall.member.exception.PhoneExistedException;
import com.mall.member.exception.UsernameExistedException;
import com.mall.member.vo.GitHubUserVo;
import com.mall.member.vo.MemberLoginVo;
import com.mall.member.vo.MemberRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 19:31:00
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistedException;

    void checkUsernameUnique(String username) throws UsernameExistedException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(GitHubUserVo vo);
}

