package com.mall.member.controller;

import com.mall.common.exception.ExceptionCodeEnum;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;
import com.mall.member.entity.MemberEntity;
import com.mall.member.exception.PhoneExistedException;
import com.mall.member.exception.UsernameExistedException;
import com.mall.member.feign.CouponFeignService;
import com.mall.member.service.MemberService;
import com.mall.member.vo.GitHubUserVo;
import com.mall.member.vo.MemberLoginVo;
import com.mall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            memberService.register(vo);
        } catch (PhoneExistedException e) {
            return R.error(ExceptionCodeEnum.PHONE_EXISTED_EXCEPTION.getCode(),
                    ExceptionCodeEnum.PHONE_EXISTED_EXCEPTION.getMessage());
        } catch (UsernameExistedException e) {
            R.error(ExceptionCodeEnum.USERNAME_EXISTED_EXCEPTION.getCode(),
                    ExceptionCodeEnum.USERNAME_EXISTED_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity member = memberService.login(vo);
        if (member != null) {
            return R.ok().put("data", member);
        } else {
            return R.error(ExceptionCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(),
                    ExceptionCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/oauth2/login")
    public R login(@RequestBody GitHubUserVo vo) {
        MemberEntity member = memberService.login(vo);
        if (member != null) {
            return R.ok();
        } else {
            return R.error(ExceptionCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(),
                    ExceptionCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION.getMessage());
        }
    }

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("Zhang San");
        R memberCoupons = couponFeignService.memberCoupons();
        return Objects.requireNonNull(R.ok().put("member", memberEntity)).put("coupons", memberCoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
