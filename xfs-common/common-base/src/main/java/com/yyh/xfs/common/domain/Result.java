package com.yyh.xfs.common.domain;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yyh
 * @date 2023-12-09
 */

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;
}
