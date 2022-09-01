package com.mall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.mall.authserver.feign.MemberFeignService;
import com.mall.authserver.vo.GitHubUserVo;
import com.mall.common.constant.AuthConstant;
import com.mall.common.utils.HttpUtils;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberRespVo;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


@Data
@Controller
@ConfigurationProperties(prefix = "oauth2.github")
public class OAuth2Controller {
    private String client_id;
    private String client_secret;

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2/github/success")
    public String login(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> queries = new HashMap<>();
        queries.put("client_id", client_id);
        queries.put("client_secret", client_secret);
        queries.put("code", code);
        HttpResponse response = HttpUtils.doGet("https://github.com", "/login/oauth/access_token", headers, queries);
        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            GitHubUserVo githubUser = new GitHubUserVo();
            githubUser.setAccessToken(json.substring(13, 53));
            R r = memberFeignService.login(githubUser);
            if (r.getCode() == 0) {
                String s = JSON.toJSONString(r.get("data"));
                MemberRespVo vo = JSON.parseObject(s, MemberRespVo.class);
                session.setAttribute(AuthConstant.LOGIN_USER, vo);
                return "redirect:http://olinmall.com";
            } else {
                return "redirect:http://auth.olinmall.com/login.html";
            }
        } else {
            return "redirect:http://auth.olinmall.com/login.html";
        }
    }
}
