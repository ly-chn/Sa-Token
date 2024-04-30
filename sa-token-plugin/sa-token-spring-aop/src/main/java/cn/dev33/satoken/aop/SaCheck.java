package cn.dev33.satoken.aop;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface SaCheck {
    /**
     * @return 在方法注解鉴权之前判断SpEL, 抛出任何异常将被捕获
     */
    String value();

    /**
     * @return 多账号体系下所属的账号体系标识，非多账号体系无需关注此值
     */
    String type() default "";
}