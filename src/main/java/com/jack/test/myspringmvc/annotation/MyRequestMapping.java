package com.jack.test.myspringmvc.annotation;

import java.lang.annotation.*;

/**
 * Created by jie on 2018/4/5.
 * 请求路由配置注解
 */
@Target(value = {ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

    /**
     * 请求的URL
     * @return
     */
    String value() default "";
}
