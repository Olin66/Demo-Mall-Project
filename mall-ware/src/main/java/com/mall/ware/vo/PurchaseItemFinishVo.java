package com.mall.ware.vo;

import lombok.Data;

@Data
public class PurchaseItemFinishVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
