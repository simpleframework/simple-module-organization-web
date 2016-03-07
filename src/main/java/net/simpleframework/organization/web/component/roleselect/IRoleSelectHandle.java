package net.simpleframework.organization.web.component.roleselect;

import java.util.Collection;

import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.IDictionaryHandle;
import net.simpleframework.organization.bean.Role;
import net.simpleframework.organization.bean.RoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IRoleSelectHandle extends IDictionaryHandle {

	/**
	 * 获取当前的角色视图，如果为null，则允许用户切换视图
	 * 
	 * @param cp
	 * @return
	 */
	RoleChart getRoleChart(ComponentParameter cp);

	/**
	 * 获取指定视图的角色列表
	 * 
	 * @param cParameter
	 * @param roleChart
	 * @param parent
	 * @return
	 */
	Collection<Role> roles(ComponentParameter cp, RoleChart roleChart, Role parent);
}
