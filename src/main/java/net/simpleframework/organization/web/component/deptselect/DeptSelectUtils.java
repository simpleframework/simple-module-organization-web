package net.simpleframework.organization.web.component.deptselect;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentUtils;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IDepartment;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class DeptSelectUtils {

	public static String icon_dept(final PageParameter pp, final IDepartment dept) {
		final String imgBase = ComponentUtils.getCssResourceHomePath(pp, DeptSelectBean.class)
				+ "/images/";
		return imgBase
				+ (dept.getDepartmentType() == EDepartmentType.organization ? "org.gif" : "dept.png");
	}
}
