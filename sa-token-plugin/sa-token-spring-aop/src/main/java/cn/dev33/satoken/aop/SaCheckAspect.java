/*
 * Copyright 2020-2099 sa-token.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.dev33.satoken.aop;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.fun.strategy.SaGetAnnotationFunction;
import cn.dev33.satoken.strategy.SaStrategy;
import cn.dev33.satoken.util.SaTokenConsts;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Sa-Token 基于 Spring Aop 的注解鉴权
 *
 * <p>
 * 注意：在打开 注解鉴权 时，AOP 模式与拦截器模式不可同时使用，否则会出现在 Controller 层重复鉴权两次的问题
 * </p>
 *
 * @author click33
 * @since 1.19.0
 */
@Aspect
@Component
@Order(SaTokenConsts.ASSEMBLY_ORDER)
public class SaCheckAspect implements BeanFactoryAware {
    /**
     * 定义AOP签名 (切入所有使用 Sa-Token 鉴权注解的方法)
     */
    public static final String POINTCUT_SIGN =
            "@within(cn.dev33.satoken.annotation.SaCheckLogin) || @annotation(cn.dev33.satoken.annotation.SaCheckLogin) || "
                    + "@within(cn.dev33.satoken.annotation.SaCheckRole) || @annotation(cn.dev33.satoken.annotation.SaCheckRole) || "
                    + "@within(cn.dev33.satoken.annotation.SaCheckPermission) || @annotation(cn.dev33.satoken.annotation.SaCheckPermission) || "
                    + "@within(cn.dev33.satoken.annotation.SaCheckSafe) || @annotation(cn.dev33.satoken.annotation.SaCheckSafe) || "
                    + "@within(cn.dev33.satoken.annotation.SaCheckDisable) || @annotation(cn.dev33.satoken.annotation.SaCheckDisable) || "
                    + "@within(cn.dev33.satoken.aop.SaCheck) || @annotation(cn.dev33.satoken.aop.SaCheck) || "
                    + "@within(cn.dev33.satoken.annotation.SaCheckHttpBasic) || @annotation(cn.dev33.satoken.annotation.SaCheckHttpBasic)";
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
    private BeanFactory beanFactory;

    /**
     * 构建
     */
    public SaCheckAspect() {
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

	/**
	 * 声明AOP签名
	 */
	@Pointcut(POINTCUT_SIGN)
	public void pointcut() {
	}

	/**
	 * 环绕切入
	 * 
	 * @param joinPoint 切面对象
	 * @return 底层方法执行后的返回值
	 * @throws Throwable 底层方法抛出的异常
	 */
	@Around("pointcut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		
		// 获取对应的 Method 处理函数 
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		
		// 如果此 Method 或其所属 Class 标注了 @SaIgnore，则忽略掉鉴权 
		if(SaStrategy.instance.isAnnotationPresent.apply(method, SaIgnore.class)) {
			// ... 
		} else {
            SaStrategy.instance.checkMethodAnnotation.accept(method);
            execSaCheck(joinPoint);
		}

		// 执行原有逻辑
		return joinPoint.proceed();
	}

    private void execSaCheck(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        SaGetAnnotationFunction getAnnotation = SaStrategy.instance.getAnnotation;
        SaCheck methodCheck = (SaCheck)getAnnotation.apply(targetMethod, SaCheck.class);
        SaCheck classCheck = (SaCheck)getAnnotation.apply(joinPoint.getTarget().getClass(), SaCheck.class);
        if (Objects.isNull(methodCheck) && Objects.isNull(classCheck)) {
            return;
        }
        Object[] args = joinPoint.getArgs();
        SaTokenRootObject rootObject = new SaTokenRootObject(
                targetMethod, extractArgs(targetMethod, args), joinPoint.getTarget(), joinPoint.getTarget().getClass(), null);
        MethodBasedEvaluationContext context =
                new MethodBasedEvaluationContext(rootObject, targetMethod, args, pnd);
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        try {
            check(methodCheck, context);
            check(classCheck, context);
        } catch (EvaluationException | ParseException e) {
            throw new SaTokenException("SaCheck失败", e);
        }
    }

    private void check(SaCheck target, MethodBasedEvaluationContext context) {
        if (target != null) {
            String classSpEL = target.value();
            if (classSpEL.isEmpty()) {
                throw new SaTokenException("SaCheck失败");
            }
            Boolean classResult = parser.parseExpression(classSpEL).getValue(context, Boolean.class);
            if (!Boolean.TRUE.equals(classResult)) {
                throw new SaTokenException("SaCheck失败");
            }
        }
    }


    /**
     * 平铺可变参
     */
    private Object[] extractArgs(Method method, Object[] args) {
        if (!method.isVarArgs()) {
            return args;
        } else {
            Object[] varArgs = ObjectUtils.toObjectArray(args[args.length - 1]);
            Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
            System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
            System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
            return combinedArgs;
        }
    }
}
