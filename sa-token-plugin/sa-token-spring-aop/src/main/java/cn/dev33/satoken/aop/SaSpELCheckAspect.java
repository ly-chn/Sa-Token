package cn.dev33.satoken.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
public class SaSpELCheckAspect implements BeanFactoryAware {
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Before("@within(cn.dev33.satoken.aop.SaCheck) || @annotation(cn.dev33.satoken.aop.SaCheck)")
    public void atBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        // 获取方法和类上的注解
        SaCheck ofMethod = AnnotationUtils.getAnnotation(targetMethod, SaCheck.class);
        SaCheck ofClazz = AnnotationUtils.getAnnotation(targetMethod.getClass(), SaCheck.class);

        // 构建校验上下文
        SaTokenRootObject rootObject = new SaTokenRootObject(targetMethod, extractArgs(targetMethod, args),
                joinPoint.getTarget(), joinPoint.getTarget().getClass(), null);
        MethodBasedEvaluationContext context =
                new MethodBasedEvaluationContext(rootObject, targetMethod, args, pnd);
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        if (ofMethod != null) {
            rootObject.setLoginType(ofMethod.type());
            check(ofMethod.value(), context);
        }
        if (ofClazz != null) {
            rootObject.setLoginType(ofClazz.type());
            check(ofClazz.value(), context);
        }
    }

    private void check(String spEl, EvaluationContext context) {
        // todo: 空文本, 算, 还是不算?
        if (!StringUtils.hasText(spEl)) {
            return;
        }
        Boolean result;
        try {
            result = parser.parseExpression(spEl).getValue(context, Boolean.class);
        } catch (EvaluationException | ParseException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            // 未知异常导致校验失败
            throw new SaCheckFailedException("未知异常导致校验失败", e);
        }
        if (Boolean.FALSE.equals(result)) {
            // 鉴权未通过导致校验失败
            throw new SaCheckFailedException("鉴权未通过导致校验失败", null);
        }
    }

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
