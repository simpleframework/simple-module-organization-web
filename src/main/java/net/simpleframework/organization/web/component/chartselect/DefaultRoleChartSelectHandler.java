package net.simpleframework.organization.web.component.chartselect;

import java.util.Collection;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.AbstractDictionaryHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.organization.IDepartment;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class DefaultRoleChartSelectHandler extends AbstractDictionaryHandler implements
		IRoleChartSelectHandle, IOrganizationContextAware {

	@Override
	public Collection<? extends IRoleChart> getRoleCharts(final ComponentParameter cp,
			final TreeBean treeBean, final IDepartment department) {
		return DataQueryUtils.toList(context.getRoleChartService().query(department));
	}

	@Override
	public Collection<? extends IDepartment> getDepartments(final ComponentParameter cp,
			final TreeBean treeBean, final IDepartment parent) {
		return DataQueryUtils.toList(context.getDepartmentService().queryChildren(parent));
	}
}
