package com.pj.demo;

import cn.dev33.satoken.annotation.*;
import cn.dev33.satoken.aop.SaCheck;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class DemoController {


    /**
     * 登录接口
     */
    @SaCheck("#name.length() > 3")
    @GetMapping("login")
    public String login(String name) {
        StpUtil.login(name);
        return "Login ok";
    }

    /**
     * 校验登录 = SaCheckLogin
     */
    @SaCheck("hasLogin()")
    @GetMapping("demo1")
    public void demo1() {
    }

    /**
     * 已登录, 或者查询数据在公开范围内
     */
    @SaCheck(value = "hasLogin() or (#id != null and #id > 100)", type = SomeConstant.agentLoginType)
    @GetMapping("demo2")
    public void demo2(Integer id) {
    }

    /**
     * 权限/角色校验
     */
    @SaCheck("hasAnyPermission('user:add', 'user:delete') and hasAnyRole('admin', 'super-admin')")
    @GetMapping("demo3")
    public void demo3(Integer id) {
    }

    /**
     * session相关校验
     */
    @SaCheck("getSession().get('user_info').username == 'liangyun' && getTokenSession().get('user_info').username == 'liangyun'")
    @GetMapping("demo4")
    public void demo4(Integer id) {
    }

    /**
     * 禁用校验
     */
    @SaCheck("hasDisabled('user', 5) and getLoginId(1L) >= 10L and isServiceSafe('some_service')")
    @GetMapping("demo5")
    public void demo5(Integer id) {
    }

    /**
     * 示例比对
     */
    @SaCheck("hasLogin() and getOtherStpLogic(@someConstant.agentLoginType).isLogin()")
    @SaCheckOr(login = {@SaCheckLogin, @SaCheckLogin(type = SomeConstant.agentLoginType)})
    @GetMapping("demo6")
    public void demo6() {}

    /**
     * 示例比对
     */
//    @SaCheckOr(
//            login = @SaCheckLogin,
//            role = @SaCheckRole("admin"),
//            permission = @SaCheckPermission("user.add"),
//            safe = @SaCheckSafe("update-password"),
//            basic = @SaCheckHttpBasic(account = "sa:123456"),
//            disable = @SaCheckDisable("submit-orders")
//    )
    @SaCheck("hasLogin() or hasAnyRole('admin') or hasPermissions('user.add') or isServiceSafe('update-password') " +
            "or 不打算支持basic('sa:123456') or hasDisabled('submit-orders', null)")
    @GetMapping("demo7")
    public void demo7() {}
}
