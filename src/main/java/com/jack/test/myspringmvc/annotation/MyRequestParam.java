package com.jack.test.myspringmvc.annotation;

import java.lang.annotation.*;

/**
 * Created by jie on 2018/4/5.
 * 请求参数注解
 */
@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRequestParam {

    /**
     * 参数的别名
     * @return
     */
    String value();
}
