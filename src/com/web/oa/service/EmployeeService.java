package com.web.oa.service;


import com.web.oa.pojo.*;

import java.util.List;

public interface EmployeeService {

	//根据员工帐号查找员工
	Employee findEmployeeByName(String name);
	
	//根据主键查找员工
	Employee findEmployeeManager(long id);
	
	List<Employee> findUsers();
	
	List<EmployeeCustom> findUserAndRoleList();
	
	void updateEmployeeRole(String roleId, String userId);
	
	List<Employee> findEmployeeByLevel(int level);

    List<SysRole> findAllRoles();

	List<MenuTree> findAllMenuAndPermission();

	List findPermissionByRoleId(String roleId);

    SysRole findRolesAndPermissionsByUserId(String userName);

    Integer deletePermission(String roleId);

    void saveUser(Employee user);
}
