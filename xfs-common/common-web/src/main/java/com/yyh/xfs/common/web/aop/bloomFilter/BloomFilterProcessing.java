package com.yyh.xfs.common.web.aop.bloomFilter;

import java.lang.annotation.*;

/**
 * @author yyh
 * @date 2024-02-24
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BloomFilterProcessing {
    String map() default "";
    String[] keys() default {};
}
