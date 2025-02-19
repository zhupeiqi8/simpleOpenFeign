package com.example.simpleOpenFeign;

import java.lang.annotation.*;

/**
 * @author zhukaijie
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignClient {

    String url() default "";

}
