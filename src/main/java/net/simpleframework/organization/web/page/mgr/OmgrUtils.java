package net.simpleframework.organization.web.page.mgr;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectEx.CacheV;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.Role;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class OmgrUtils implements IOrganizationContextAware {

	public static Role getRole(final PageRequestResponse rRequest) {
		final String roleId = rRequest.getParameter("roleId");
		if (!StringUtils.hasText(roleId)) {
			return null;
		}
		return rRequest.getRequestCache(roleId, new CacheV<Role>() {
			@Override
			public Role get() {
				return _roleService.getBean(roleId);
			}
		});
	}
}
