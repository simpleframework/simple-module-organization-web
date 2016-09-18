package net.simpleframework.organization.web.component.roleselect;

import java.util.Collection;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.common.coll.CollectionUtils;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.AbstractDictionaryHandler;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.bean.Role;
import net.simpleframework.organization.bean.RoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultRoleSelectHandler extends AbstractDictionaryHandler
		implements IRoleSelectHandle, IOrganizationContextAware {

	@Override
	public Collection<Role> roles(final ComponentParameter cp, final RoleChart roleChart,
			final Role parent) {
		if (roleChart == null) {
			return CollectionUtils.EMPTY_LIST();
		}
		return DataQueryUtils.toList(parent == null ? _roleService.queryRoot(roleChart)
				: _roleService.queryChildren(parent));
	}

	@Override
	public RoleChart getRoleChart(final ComponentParameter cp) {
		return null;
	}
}
