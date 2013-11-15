package net.simpleframework.organization.web.component.deptselect;

import java.util.Collection;

import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.IDictionaryHandle;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.organization.IDepartment;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IDeptSelectHandle extends IDictionaryHandle {

	/**
	 * 获取机构树数据
	 * 
	 * @param cParameter
	 * @param treeBean
	 * @param parent
	 * @return
	 */
	Collection<? extends IDepartment> getDepartments(ComponentParameter cp, TreeBean treeBean,
			IDepartment parent);
}
