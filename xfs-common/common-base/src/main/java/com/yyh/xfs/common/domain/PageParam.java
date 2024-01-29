package com.yyh.xfs.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2024-01-29
 */
@Data
public class PageParam implements Serializable {
    private Integer page;
    private Integer pageSize;
    private Double longitude;
    private Double latitude;
    private String keyword;
}
