package com.web.oa.service.impl;

import com.web.oa.mapper.SysPermissionMapper;
import com.web.oa.mapper.SysPermissionMapperCustom;
import com.web.oa.mapper.SysRoleMapper;
import com.web.oa.mapper.SysRolePermissionMapper;
import com.web.oa.pojo.*;
import com.web.oa.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class SysServiceImpl implements SysService {

    @Autowired
    private SysPermissionMapperCustom sysPermissionMapperCustom;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public List<SysPermission> findMenuListByUserId(String userid) throws Exception {
        return null;
    }

    @Override
    public List<SysPermission> findPermissionListByUserId(String userid) throws Exception {
        return sysPermissionMapperCustom.findPermissionListByUserId(userid);
    }

    @Override
    public List<MenuTree> loadMenuTree() {
        return sysPermissionMapperCustom.getMenuTree();
    }

    @Override
    public List<SysRole> findAllRoles() {
        return null;
    }

    @Override
    public SysRole findRolesAndPermissionsByUserId(String userId) {
        return null;
    }

    @Override
    public void addRoleAndPermissions(SysRole role, int[] permissionIds) {
        sysRoleMapper.insert(role);
        for (int permissionId : permissionIds) {
            String uuid = UUID.randomUUID().toString();
            SysRolePermission sysRolePermission = new SysRolePermission();
            sysRolePermission.setId(uuid);
            sysRolePermission.setSysRoleId(role.getId());
            sysRolePermission.setSysPermissionId(permissionId + "");
            sysRolePermissionMapper.insert(sysRolePermission);
        }
    }

    @Override
    public List<SysPermission> findAllMenus() {
        SysPermissionExample example = new SysPermissionExample();
        SysPermissionExample.Criteria criteria = example.createCriteria();
        criteria.andTypeEqualTo("menu");
        return sysPermissionMapper.selectByExample(example);
    }

    @Override
    public void addSysPermission(SysPermission permission) {
        sysPermissionMapper.insert(permission);
    }

    @Override
    public List<SysPermission> findMenuAndPermissionByUserId(String userId) {
        return null;
    }

    @Override
    public List<MenuTree> getAllMenuAndPermission() {
        return null;
    }

    @Override
    public List<SysPermission> findPermissionsByRoleId(String roleId) {
        return null;
    }

    @Override
    public void updateRoleAndPermissions(String roleId, int[] permissionIds) {

    }

    @Override
    public List<SysRole> findRolesAndPermissions() {
        return null;
    }
}
