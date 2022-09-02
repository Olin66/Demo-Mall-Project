package com.mall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartVo {
    private List<CartItemVo> items;
    private Integer countNum;
    private Integer countType;
    private BigDecimal totalAmount;
    private BigDecimal reduce = BigDecimal.ZERO;

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        countNum = 0;
        if (items != null && !items.isEmpty()) {
            for (CartItemVo item : items) {
                countNum += item.getCount();
            }
        }
        return countNum;
    }

    public Integer getCountType() {
        countType = 0;
        if (items != null && !items.isEmpty()) {
            countType = items.size();
        }
        return countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal(0);
        if (items != null && !items.isEmpty()) {
            for (CartItemVo item : items) {
                amount = amount.add(item.getTotalPrice());
            }
        }
        amount = amount.subtract(getReduce());
        return amount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
