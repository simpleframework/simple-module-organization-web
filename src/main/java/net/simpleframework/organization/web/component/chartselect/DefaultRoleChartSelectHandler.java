package net.simpleframework.organization.web.component.chartselect;

import java.util.Collection;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.AbstractDictionaryHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.bean.Department;
import net.simpleframework.organization.bean.Department.EDepartmentType;
import net.simpleframework.organization.bean.RoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultRoleChartSelectHandler extends AbstractDictionaryHandler
		implements IRoleChartSelectHandle, IOrganizationContextAware {

	@Override
	public Collection<RoleChart> getRoleCharts(final ComponentParameter cp, final TreeBean treeBean,
			final Department dept) {
		final IDataQuery<RoleChart> dq = dept == null ? _rolecService.queryGlobalCharts()
				: _rolecService.queryOrgCharts(dept);
		return DataQueryUtils.toList(dq);
	}

	@Override
	public Collection<Department> getDepartments(final ComponentParameter cp,
			final TreeBean treeBean, final Department parent) {
		return DataQueryUtils
				.toList(_deptService.queryDepartments(parent, EDepartmentType.organization));
	}
}
