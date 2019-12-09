package com.web.oa.controller;

import com.web.oa.pojo.*;
import com.web.oa.service.EmployeeService;
import com.web.oa.service.SysService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private SysService sysService;

    @RequestMapping("/login")
    public String login(HttpServletRequest request, Model model){

        String exceptionName = (String) request.getAttribute("shiroLoginFailure");
        if (exceptionName != null) {
            if (UnknownAccountException.class.getName().equals(exceptionName)) {
                model.addAttribute("errorMsg", "用户账号不存在");
            } else if (IncorrectCredentialsException.class.getName().equals(exceptionName)) {
                model.addAttribute("errorMsg", "密码不正确");
            } 
            else {
                model.addAttribute("errorMsg", "含有非法字符");
            }
        }
        return "login";
    }

    @RequestMapping("/main")
    public String main(ModelMap model) {
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("activeUser", activeUser);
        return "index";
    }

    @RequestMapping("/findRoles")
    public String findRoles(Model model){
        //获取当前用户
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        //获取所有的角色
        List<SysRole> roles = employeeService.findAllRoles();
        //获取所有的角色以及权限
        List<MenuTree> allMenuAndPermission = employeeService.findAllMenuAndPermission();
        model.addAttribute("allRoles", roles);

        model.addAttribute("allMenuAndPermissions", allMenuAndPermission);
        return "permissionlist";
    }


    @RequestMapping("/loadMyPermissions")
    @ResponseBody
    public List myPermissions(String roleId){
        List<SysPermission> permissions = employeeService.findPermissionByRoleId(roleId);
        return permissions;
    }


    //查询所有用户和角色
    @RequestMapping("/findUserList")
    public String findUserList(Model model){
        List<SysRole> allRoles = employeeService.findAllRoles();
        List<EmployeeCustom> employees = employeeService.findUserAndRoleList();
        model.addAttribute("userList", employees);
        model.addAttribute("allRoles", allRoles);
        return "userlist";
    }

    //查询选中用户所拥有的权限
    @RequestMapping("/viewPermissionByUser")
    @ResponseBody
    public SysRole findPermissionByUser(String userName){
        return employeeService.findRolesAndPermissionsByUserId(userName);
    }

    //跳转到添加角色页面
    @RequestMapping("/toAddRole")
    public String toAddRole(Model model) {
        List<MenuTree> allPermissions = sysService.loadMenuTree();
        List<SysPermission> menus = sysService.findAllMenus();
        List<SysRole> permissionList = sysService.findRolesAndPermissions();
        model.addAttribute("allPermissions", allPermissions);
        model.addAttribute("menuTypes", menus);
        return "rolelist";
    }

    //更改用户角色功能
    @RequestMapping("/assignRole")
    @ResponseBody
    public Map<String, String> assignRole(String roleId, String userId){
        Map<String, String> map = new HashMap<>();
        try {
            employeeService.updateEmployeeRole(roleId, userId);
            map.put("msg", "分配权限成功");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("msg", "分配权限失败");
        }
        return map;
    }


    //创建用户
    @RequestMapping("/saveUser")
    public String saveUser(Employee user){
        employeeService.saveUser(user);
        return "redirect:/findUserList";
    }

    //新建权限
    @RequestMapping("/saveSubmitPermission")
    public String saveSubmitPermission(SysPermission sysPermission){
        if (sysPermission.getAvailable()==null){
            sysPermission.setAvailable("0");
        }
        sysService.addSysPermission(sysPermission);
        return "redirect:/toAddRole";
    }

    //新建角色并添加权限
    @RequestMapping("/saveRoleAndPermissions")
    public String saveRoleAndPermissions(SysRole role,int []permissionIds){
        String roleId = UUID.randomUUID().toString();
        role.setId(roleId);
        role.setAvailable("1");
        sysService.addRoleAndPermissions(role, permissionIds);
        return "redirect:/toAddRole";
    }

    //删除角色
    @RequestMapping("/deletePermission")
    public String deletePermission(String roleId){
        Integer i = employeeService.deletePermission(roleId);
        return "redirect:/findRoles";
    }

}
