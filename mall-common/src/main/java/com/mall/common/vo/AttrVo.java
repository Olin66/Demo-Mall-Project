package com.mall.common.vo;


import lombok.Data;

@Data
public class AttrVo {
    private static final long serialVersionUID = 1L;
    private Long attrId;
    private String attrName;
    private Integer searchType;
    private Integer valueType;
    private String icon;
    private String valueSelect;
    private Integer attrType;
    private Long enable;
    private Long catelogId;
    private Integer showDesc;
    private Long attrGroupId;
}
