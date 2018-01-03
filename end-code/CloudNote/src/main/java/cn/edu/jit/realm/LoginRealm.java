package cn.edu.jit.realm;

import cn.edu.jit.entry.Login;
import cn.edu.jit.entry.Role;
import cn.edu.jit.util.Sha1Utils;
import cn.edu.jit.service.LoginService;
import cn.edu.jit.service.RoleService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * 身份验证
 * @author jitwxs
 * @date 2018/1/2 19:24
 */
@Component
public class LoginRealm extends AuthorizingRealm{

    @Resource(name = "loginServiceImpl")
    private LoginService loginService;

    @Resource(name = "roleServiceImpl")
    private RoleService roleService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String tel = (String) getAvailablePrincipal(principalCollection);

        Role role = null;

        try {
            Login login = loginService.getByTel(tel);
            // 获取角色对象
            role = roleService.getById(login.getRoleId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //通过用户名从数据库获取权限/角色信息
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Set<String> r = new HashSet<String>();
        if (role != null) {
            r.add(role.getName());
            info.setRoles(r);
        }
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // 获取用户名和密码
        String tel = (String) token.getPrincipal();
        String password = new String((char[])token.getCredentials());

        Login login = null;
        try {
            login = loginService.getByTel(tel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 登录失败
        if (login == null || !Sha1Utils.validatePassword(password, login.getPassword())) {
           throw new IncorrectCredentialsException("登录失败，用户名或密码不正确！");
        }

        // 身份验证通过,返回一个身份信息
        AuthenticationInfo info = new SimpleAuthenticationInfo(tel,password,getName());

        return info;
    }
}
