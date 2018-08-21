package pers.minispringmvc.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YRequestParam {
    /**
     * 表示参数的别名，必填
     * @return
     */
    String value();
}
