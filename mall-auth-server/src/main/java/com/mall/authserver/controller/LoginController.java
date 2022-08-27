package com.mall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.mall.authserver.feign.MemberFeignService;
import com.mall.authserver.feign.ThirdPartyFeignService;
import com.mall.authserver.vo.UserLoginVo;
import com.mall.authserver.vo.UserRegisterVo;
import com.mall.common.constant.AuthConstant;
import com.mall.common.exception.ExceptionCodeEnum;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberRespVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (redisCode != null) {
            long l = Long.parseLong(redisCode.substring(7));
            if (System.currentTimeMillis() - l < 60000) {
                return R.error(ExceptionCodeEnum.SMS_CODE_EXCEPTION.getCode(),
                        ExceptionCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 6);
        thirdPartyFeignService.sendCode(phone, code);
        code = code + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, code, 5, TimeUnit.MINUTES);
        return R.ok();
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,
                    FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.olinmall.com/reg.html";
        }
        String code = vo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {
            if (code.equals(redisCode.substring(0, 6))) {
                redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                R r = memberFeignService.register(vo);
                if (r.getCode() != 0) {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.get("msg").toString());
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.olinmall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.olinmall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.olinmall.com/reg.html";
        }
        return "redirect:http://auth.olinmall.com/login.html";
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        R r = memberFeignService.login(vo);
        if (r.getCode() == 0){
            String s = JSON.toJSONString(r.get("data"));
            MemberRespVo data = JSON.parseObject(s, MemberRespVo.class);
            session.setAttribute("user", data);
            return "redirect:http://olinmall.com";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.get("msg").toString());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.olinmall.com/login.html";
        }
    }
}
