package com.yyh.xfs.common.domain;

import lombok.*;
import java.io.Serializable;

/**
 * @author yyh
 * @date 2023-12-11
 * @desc 返回结果
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;
}
