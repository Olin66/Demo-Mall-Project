package com.mall.authserver.feign;

import com.mall.authserver.vo.GitHubUserVo;
import com.mall.authserver.vo.UserLoginVo;
import com.mall.authserver.vo.UserRegisterVo;
import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R login(@RequestBody GitHubUserVo vo);
}
