package cn.dev33.satoken.aop;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dev33.satoken.util.SaTokenConsts;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class SaTokenRootObject {
    private final Method method;
    private final Object[] args;
    private final Object target;
    private final Class<?> targetClass;
    private String loginType;

    public SaTokenRootObject(Method method, Object[] args, Object target, Class<?> targetClass, String loginType) {
        this.method = method;
        this.args = args;
        this.target = target;
        this.targetClass = targetClass;
        this.loginType = loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    /**
     * @return 方法名称
     */
    public String getMethodName() {
        return this.method.getName();
    }

    /**
     * @return 方法
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return 参数(会自动处理扁平化)
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * @return 目标对象
     */
    public Object getTarget() {
        return target;
    }

    /**
     * @return 目标对象类型
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * @return StpLogic对象(基于当前loginType)
     */
    public StpLogic getStpLogic() {
        return SaManager.getStpLogic(loginType);
    }

    /**
     * @return StpLogic对象(基于指定loginType)
     */
    public StpLogic getOtherStpLogic(String loginType) {
        return SaManager.getStpLogic(loginType);
    }

    /**
     * 验证任一角色
     *
     * @see StpLogic#hasRoleOr(String...)
     */
    public boolean hasAnyRole(String... roles) {
        return getStpLogic().hasRoleOr(roles);
    }

    /**
     * 验证所有角色
     *
     * @see StpLogic#hasRoleAnd(String...)
     */
    public boolean hasRoles(String... roles) {
        return getStpLogic().hasRoleAnd(roles);
    }

    /**
     * 验证任一权限
     *
     * @see StpLogic#hasPermissionOr(String...)
     */
    public boolean hasAnyPermission(String... permissions) {
        return getStpLogic().hasPermissionOr(permissions);
    }

    /**
     * 验证所有权限
     *
     * @see StpLogic#hasPermissionAnd(String...)
     */
    public boolean hasPermissions(String... permissions) {
        return getStpLogic().hasPermissionAnd(permissions);
    }

    /**
     * 获取当前已登录账号的 Account-Session
     *
     * @see StpLogic#getSession()
     */
    public SaSession getSession() {
        return getStpLogic().getSession();
    }

    /**
     * 获取当前 token 的 Token-Session
     *
     * @see StpLogic#getTokenSession()
     */
    public SaSession getTokenSession() {
        return getStpLogic().getTokenSession();
    }

    /**
     * 判断当前会话是否已经登录
     *
     * @see StpLogic#isLogin()
     */
    public boolean hasLogin() {
        return getStpLogic().isLogin();
    }

    /**
     * 获取当前会话账号id，默认乃是为了方便SpEL代码提示, 不能为空
     *
     * @see StpLogic#getLoginId(T)
     */
    public <T> T getLoginId(@NonNull T defaultValue) {
        return getStpLogic().getLoginId(defaultValue);
    }

    /**
     * 服务禁用校验
     *
     * @see SaTokenConsts#DEFAULT_DISABLE_SERVICE
     * @see SaTokenConsts#DEFAULT_DISABLE_LEVEL
     * @see StpLogic#isDisableLevel(Object, String, int)
     */
    public boolean hasDisabled(String service, Integer level) {
        StpLogic stpLogic = getStpLogic();
        if (SaFoxUtil.isEmpty(service)) {
            service = SaTokenConsts.DEFAULT_DISABLE_SERVICE;
        }
        if (level == null) {
            level = SaTokenConsts.DEFAULT_DISABLE_LEVEL;
        }
        return stpLogic.isDisableLevel(stpLogic.getLoginId(), service, level);
    }

    /**
     * 二级认证校验
     *
     * @see StpLogic#isSafe(String)
     */
    public boolean isServiceSafe(String service) {
        if (SaFoxUtil.isEmpty(service)) {
            service = SaTokenConsts.DEFAULT_SAFE_AUTH_SERVICE;
        }
        return getStpLogic().isSafe(service);
    }

    /**
     * 二级认证校验
     *
     * @see StpLogic#isSafe(String)
     */
    public boolean isSafe() {
        return getStpLogic().isSafe(SaTokenConsts.DEFAULT_SAFE_AUTH_SERVICE);
    }

}
