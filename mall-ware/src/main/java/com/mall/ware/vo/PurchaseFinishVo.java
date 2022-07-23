package com.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseFinishVo {
    @NotNull
    private Long id;
    List<PurchaseItemFinishVo> items;
}
