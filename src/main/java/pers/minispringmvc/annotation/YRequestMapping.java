package pers.minispringmvc.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YRequestMapping {
    /**
     * 表示参数的别名，必填
     * @return
     */
    String value();
}
