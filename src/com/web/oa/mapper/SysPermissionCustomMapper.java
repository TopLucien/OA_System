package com.web.oa.mapper;


import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.TreeMenu;

import java.util.List;

public interface SysPermissionCustomMapper {

	
	public List<TreeMenu> getTreeMenu();
	
	public List<SysPermission> getSubMenu(int id);
}
