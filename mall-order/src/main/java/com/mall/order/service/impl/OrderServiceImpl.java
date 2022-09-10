package com.mall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.OrderConstant;
import com.mall.common.to.SkuHasStockTo;
import com.mall.common.to.mq.OrderTo;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberRespVo;
import com.mall.order.dao.OrderDao;
import com.mall.order.entity.OrderEntity;
import com.mall.order.entity.OrderItemEntity;
import com.mall.order.enume.OrderStatusEnum;
import com.mall.order.feign.CartFeignService;
import com.mall.order.feign.MemberFeignService;
import com.mall.order.feign.ProductFeignService;
import com.mall.order.feign.WareFeignService;
import com.mall.order.interceptor.LoginUserInterceptor;
import com.mall.order.service.OrderItemService;
import com.mall.order.service.OrderService;
import com.mall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo vo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderAddressVo> addresses = memberFeignService.getAddresses(memberRespVo.getId());
            vo.setAddresses(addresses);
        }, executor);
        CompletableFuture<Void> itemTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            vo.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = vo.getItems();
            List<Long> list = items.stream().map(OrderItemVo::getSkuId).toList();
            R r = wareFeignService.getSkusHasStock(list);
            String json = JSON.toJSONString(r.get("data"));
            JSONArray array = JSONArray.parseArray(json);
            List<SkuHasStockTo> data = array.toJavaList(SkuHasStockTo.class);
            if (data != null) {
                Map<Long, Boolean> map = data.stream()
                        .collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                vo.setStocks(map);
            }
        }, executor);
        CompletableFuture<Void> integrationTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            Integer integration = memberRespVo.getIntegration();
            vo.setIntegration(integration);
        }, executor);
        String token = UUID.randomUUID().toString().replace("-", "");
        vo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(),
                token, 30, TimeUnit.MINUTES);
        CompletableFuture.allOf(addressTask, itemTask, integrationTask).get();
        return vo;
    }

    @Override
    @Transactional
    public OrderSubmitRespVo submitOrder(OrderSubmitVo vo) {
        submitVoThreadLocal.set(vo);
        OrderSubmitRespVo resp = new OrderSubmitRespVo();
        resp.setCode(0);
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String formToken = vo.getOrderToken();
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                List.of(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                formToken);
        if (result == null || result == 0L) {
            resp.setCode(1);
        } else {
            OrderCreateVo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            double abs = Math.abs(payAmount.subtract(payPrice).doubleValue());
            if (abs < 0.01) {
                saveOrder(order);
                OrderSkuLockVo lockVo = new OrderSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).toList();
                lockVo.setLocks(locks);
                R r = wareFeignService.orderLockStock(lockVo);
                if (r.getCode() != 0) {
                    resp.setCode(3);
                } else {
                    resp.setOrder(order.getOrder());
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order);
                }
            } else {
                resp.setCode(2);
            }
        }
        return resp;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderCreateVo vo) {
        OrderEntity order = vo.getOrder();
        if (order != null && Objects.equals(order.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            OrderEntity temp = new OrderEntity();
            temp.setId(order.getId());
            temp.setStatus(OrderStatusEnum.CANCELED.getCode());
            this.updateById(temp);
            OrderTo to = new OrderTo();
            BeanUtils.copyProperties(order, to);
            to.setStatus(OrderStatusEnum.CANCELED.getCode());
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", to);
        }
    }

    private void saveOrder(OrderCreateVo order) {
        OrderEntity orderEntity = order.getOrder();
        List<OrderItemEntity> orderItems = order.getOrderItems();
        this.save(orderEntity);
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateVo createOrder() {
        OrderCreateVo vo = new OrderCreateVo();
        String orderSn = IdWorker.getTimeId();
        OrderEntity entity = buildOrderEntity(orderSn);
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        computePrice(entity, orderItemEntities);
        vo.setOrder(entity);
        vo.setOrderItems(orderItemEntities);
        return vo;
    }

    private void computePrice(OrderEntity entity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal(0);
        BigDecimal coupon = new BigDecimal(0);
        BigDecimal integration = new BigDecimal(0);
        BigDecimal promotion = new BigDecimal(0);
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;
        if (orderItemEntities != null) {
            for (OrderItemEntity orderItemEntity : orderItemEntities) {
                BigDecimal realAmount = orderItemEntity.getRealAmount();
                BigDecimal couponAmount = orderItemEntity.getCouponAmount();
                BigDecimal integrationAmount = orderItemEntity.getIntegrationAmount();
                BigDecimal promotionAmount = orderItemEntity.getPromotionAmount();
                total = total.add(realAmount);
                coupon = coupon.add(couponAmount);
                integration = integration.add(integrationAmount);
                promotion = promotion.add(promotionAmount);
                giftIntegration += orderItemEntity.getGiftIntegration();
                giftGrowth += orderItemEntity.getGiftGrowth();
            }
        }
        entity.setTotalAmount(total);
        entity.setPayAmount(total.add(entity.getFreightAmount()));
        entity.setCouponAmount(coupon);
        entity.setIntegrationAmount(integration);
        entity.setPromotionAmount(promotion);
        entity.setIntegration(giftIntegration);
        entity.setGrowth(giftGrowth);
        entity.setDeleteStatus(0);
    }

    private OrderEntity buildOrderEntity(String orderSn) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        OrderSubmitVo submitVo = submitVoThreadLocal.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(memberRespVo.getId());
        entity.setMemberUsername(memberRespVo.getUsername());
        OrderFareVo fareVo = wareFeignService.getFare(submitVo.getAddrId());
        entity.setFreightAmount(fareVo.getFare());
        entity.setReceiverName(fareVo.getAddress().getName());
        entity.setReceiverPhone(fareVo.getAddress().getPhone());
        entity.setReceiverProvince(fareVo.getAddress().getProvince());
        entity.setReceiverRegion(fareVo.getAddress().getRegion());
        entity.setReceiverCity(fareVo.getAddress().getCity());
        entity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        entity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null) {
            return currentUserCartItems.stream().map(item -> {
                OrderItemEntity entity = buildOrderItem(item);
                entity.setOrderSn(orderSn);
                return entity;
            }).toList();
        } else return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity entity = new OrderItemEntity();
        Long skuId = item.getSkuId();
        entity.setSkuId(skuId);
        entity.setSkuName(item.getTitle());
        entity.setSkuPic(item.getImage());
        entity.setSkuPrice(item.getPrice());
        entity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";"));
        entity.setSkuQuantity(item.getCount());
        entity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        entity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        OrderSpuInfoVo spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        entity.setSpuId(spuInfo.getId());
        entity.setSpuBrand(spuInfo.getBrandId().toString());
        entity.setSpuName(spuInfo.getSpuName());
        entity.setCategoryId(spuInfo.getCatalogId());
        entity.setPromotionAmount(new BigDecimal(0));
        entity.setCouponAmount(new BigDecimal(0));
        entity.setIntegrationAmount(new BigDecimal(0));
        BigDecimal origin = entity.getSkuPrice().multiply(new BigDecimal(entity.getSkuQuantity()));
        entity.setRealAmount(origin
                .subtract(entity.getPromotionAmount())
                .subtract(entity.getCouponAmount())
                .subtract(entity.getIntegrationAmount()));
        return entity;
    }

}