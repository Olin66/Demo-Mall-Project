package com.mall.order.web;

import com.mall.order.service.OrderService;
import com.mall.order.vo.OrderConfirmVo;
import com.mall.order.vo.OrderSubmitRespVo;
import com.mall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class WebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page) {
        return page;
    }

    @GetMapping("toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo vo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", vo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes attributes) {
        OrderSubmitRespVo resp = orderService.submitOrder(vo);
        if (resp.getCode() == 0) {
            model.addAttribute("submitOrderResp", resp);
            return "pay";
        } else {
            String msg = "下单失败：";
            switch (resp.getCode()) {
                case 1 -> msg += "订单信息过期，请刷新再次提交";
                case 2 -> msg += "订单商品价格发生变化，请确认后再次提交";
                case 3 -> msg += "库存锁定失败，商品库存不足";
            }
            attributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.olinmall.com/toTrade";
        }
    }
}
