package net.simpleframework.organization.web.component.chartselect;

import java.util.Collection;

import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.IDictionaryHandle;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.organization.IDepartment;
import net.simpleframework.organization.IRoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IRoleChartSelectHandle extends IDictionaryHandle {

	/**
	 * 获取角色试图
	 * 
	 * @param cParameter
	 * @param treeBean
	 * @param department
	 *           department==null返回全局视图
	 * @return
	 */
	Collection<? extends IRoleChart> getRoleCharts(ComponentParameter cp, TreeBean treeBean,
			IDepartment department);

	/**
	 * 获取部门树
	 * 
	 * @param cParameter
	 * @param treeBean
	 * @param parent
	 * @return
	 */
	Collection<? extends IDepartment> getDepartments(ComponentParameter cp, TreeBean treeBean,
			IDepartment parent);
}
