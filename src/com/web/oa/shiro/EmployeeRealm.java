package com.web.oa.shiro;
import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.Employee;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.service.EmployeeService;
import com.web.oa.service.SysService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;


public class EmployeeRealm extends AuthorizingRealm {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private SysService sysService;

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //获取token中的用户名
        String username = (String) authenticationToken.getPrincipal();
        //根据用户名获取数据库中的用户对象
        Employee employee = employeeService.findEmployeeByName(username);
        if (employee!=null){
            List<MenuTree> menuTree = sysService.loadMenuTree();
            //把用户的身份信息重新封装
            ActiveUser activeUser = new ActiveUser();
            activeUser.setId(employee.getId());
            activeUser.setUserid(employee.getName());
            activeUser.setUsercode(employee.getName());
            activeUser.setUsername(employee.getName());
            activeUser.setManagerId(employee.getManagerId());
            activeUser.setMenuTree(menuTree);
            String password_db = employee.getPassword();    // 数据库中的密码,密文
            System.out.println(password_db);
            String salt = employee.getSalt();
            System.out.println(salt);

            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(activeUser, password_db, ByteSource.Util.bytes(salt), "EmployeeRealm");
            return info;
        }
        return null;
    }


    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        ActiveUser activeUser = (ActiveUser) principalCollection.getPrimaryPrincipal();
        //查询数据库认证用户拥有的角色和权限
        List<SysPermission> permissions = null;
        try {
            permissions = sysService.findPermissionListByUserId(activeUser.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> permisionList = new ArrayList<>();
        for (SysPermission sysPermission : permissions) {
            permisionList.add(sysPermission.getPercode());
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addStringPermissions(permisionList);

        return info;
    }
}
