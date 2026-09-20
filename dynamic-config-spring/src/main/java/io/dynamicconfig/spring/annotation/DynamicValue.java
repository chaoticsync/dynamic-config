package io.dynamicconfig.spring.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicValue {

    /**
     * Configuration key.
     */
    String value();

}