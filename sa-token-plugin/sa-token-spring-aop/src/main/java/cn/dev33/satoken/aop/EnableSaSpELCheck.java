package cn.dev33.satoken.aop;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 在spring security 中, 使用 EnableMethodSecurity 注解来启用注解鉴权, 可配置AOP/Proxy方式, 且支持一些特殊配置
 * @author ly
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Configuration
@Import(SaSpELCheckAspect.class)
public @interface EnableSaSpELCheck {
}
