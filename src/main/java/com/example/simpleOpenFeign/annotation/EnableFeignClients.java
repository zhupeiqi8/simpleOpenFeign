package com.example.simpleOpenFeign.annotation;

import com.example.simpleOpenFeign.FeignClientsRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author zhukaijie
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(FeignClientsRegistrar.class)
public @interface EnableFeignClients {
    String[] basePackages() default {};
}
