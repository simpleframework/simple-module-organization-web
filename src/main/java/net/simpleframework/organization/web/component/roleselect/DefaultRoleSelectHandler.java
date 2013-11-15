package net.simpleframework.organization.web.component.roleselect;

import java.util.Collection;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.AbstractDictionaryHandler;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRole;
import net.simpleframework.organization.IRoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultRoleSelectHandler extends AbstractDictionaryHandler implements
		IRoleSelectHandle, IOrganizationContextAware {

	@Override
	public Collection<? extends IRole> roles(final ComponentParameter cp,
			final IRoleChart roleChart, final IRole parent) {
		return DataQueryUtils.toList(parent == null ? context.getRoleService().queryRoot(roleChart)
				: context.getRoleService().queryChildren(parent));
	}
}
