package com.web.oa.service.impl;


import com.web.oa.mapper.EmployeeMapper;
import com.web.oa.mapper.SysPermissionMapperCustom;
import com.web.oa.mapper.SysRoleMapper;
import com.web.oa.mapper.SysUserRoleMapper;
import com.web.oa.pojo.*;
import com.web.oa.service.EmployeeService;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysPermissionMapperCustom sysPermissionMapperCustom;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public Employee findEmployeeByName(String name) {
        EmployeeExample employeeExample = new EmployeeExample();
        EmployeeExample.Criteria criteria = employeeExample.createCriteria();
        criteria.andNameEqualTo(name);
        List<Employee> employees = employeeMapper.selectByExample(employeeExample);
        return employees.get(0);
    }

    @Override
    public Employee findEmployeeManager(long id) {
        return employeeMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Employee> findUsers() {
        return employeeMapper.selectByExample(null);
    }

    @Override
    public List<EmployeeCustom> findUserAndRoleList() {
        return sysPermissionMapperCustom.findUserAndRoleList();
    }

    @Override
    public void updateEmployeeRole(String roleId, String userId) {
        SysUserRoleExample sysUserRoleExample = new SysUserRoleExample();
        sysUserRoleExample.createCriteria().andSysUserIdEqualTo(userId);
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectByExample(sysUserRoleExample);
        SysUserRole sysUserRole = sysUserRoles.get(0);
        sysUserRole.setSysRoleId(roleId);
        sysUserRoleMapper.updateByPrimaryKey(sysUserRole);
    }

    @Override
    public List<Employee> findEmployeeByLevel(int level) {
        return null;
    }

    @Override
    public List<SysRole> findAllRoles() {
        return  sysRoleMapper.selectByExample(null);
    }

    @Override
    public List<MenuTree> findAllMenuAndPermission() {
        return sysPermissionMapperCustom.getAllMenuAndPermision();
    }

    @Override
    public List findPermissionByRoleId(String roleId) {
        return sysPermissionMapperCustom.findPermissionsByRoleId(roleId);
    }


    @Override
    public SysRole findRolesAndPermissionsByUserId(String userName) {
        return sysPermissionMapperCustom.findRoleAndPermissionListByUserId(userName);
    }

    @Override
    public Integer deletePermission(String roleId) {
        int i = sysRoleMapper.deleteByPrimaryKey(roleId);
        return i;
    }

    @Override
    public void saveUser(Employee user) {
        user.setSalt("eteokues");
        Md5Hash md5Hash = new Md5Hash(user.getPassword(),user.getSalt(),2);
        user.setPassword(md5Hash.toString());
        this.employeeMapper.insert(user);
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setId(UUID.randomUUID().toString());
        sysUserRole.setSysUserId(user.getName());
        sysUserRole.setSysRoleId(user.getRole().toString());
        this.sysUserRoleMapper.insert(sysUserRole);
    }
}
