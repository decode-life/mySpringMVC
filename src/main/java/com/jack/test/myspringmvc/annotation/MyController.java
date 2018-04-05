package com.jack.test.myspringmvc.annotation;

import java.lang.annotation.*;

/**
 * Created by jie on 2018/4/5.
 *
 * Controller 注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {

    /**
     * 控制器的别名
     * @return
     */
    String value() default "";
}
