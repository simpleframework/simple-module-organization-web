package net.simpleframework.organization.web.component.roleselect;

import java.util.Collection;

import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.IDictionaryHandle;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IRoleSelectHandle extends IDictionaryHandle {

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
