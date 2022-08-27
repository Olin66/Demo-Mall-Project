package com.mall.member.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.HttpUtils;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.member.dao.MemberDao;
import com.mall.member.dao.MemberLevelDao;
import com.mall.member.entity.MemberEntity;
import com.mall.member.entity.MemberLevelEntity;
import com.mall.member.exception.PhoneExistedException;
import com.mall.member.exception.UsernameExistedException;
import com.mall.member.service.MemberService;
import com.mall.member.vo.GitHubUserInfoVo;
import com.mall.member.vo.GitHubUserVo;
import com.mall.member.vo.MemberLoginVo;
import com.mall.member.vo.MemberRegisterVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String account = vo.getAccount();
        String password = vo.getPassword();
        MemberEntity member = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", account).or().eq("mobile", account));
        if (member == null) return null;
        String pwdDb = member.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(password, pwdDb);
        return matches ? member : null;
    }

    @Override
    public MemberEntity login(GitHubUserVo vo) {
        String accessToken = vo.getAccessToken();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "token " + accessToken);
        HttpResponse response;
        try {
            response = HttpUtils.doGet("https://api.github.com", "/user", headers, null);
            String json = EntityUtils.toString(response.getEntity());
            GitHubUserInfoVo info = JSON.parseObject(json, GitHubUserInfoVo.class);
            String username = info.getLogin();
            MemberEntity member = this.baseMapper
                    .selectOne(new QueryWrapper<MemberEntity>().eq("github_username", username));
            if (member == null) {
                member = new MemberEntity();
                String email = info.getEmail();
                String city = info.getLocation();
                String name = info.getName();
                member.setEmail(email);
                member.setCity(city);
                member.setNickname(name);
                member.setGender(0);
                member.setGithubUsername(username);
                MemberLevelEntity level = memberLevelDao.getDefaultLevel();
                member.setLevelId(level.getId());
                member.setAccessToken(accessToken);
                this.baseMapper.insert(member);
            }
            return member;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}