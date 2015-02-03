package net.simpleframework.organization.web;

import net.simpleframework.mvc.common.UrlsCache;
import net.simpleframework.organization.web.page2.DepartmentMgrTPage;
import net.simpleframework.organization.web.page2.RoleMgrTPage;
import net.simpleframework.organization.web.page2.UserMgrTPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class OrganizationUrlsFactory extends UrlsCache {

	public OrganizationUrlsFactory() {
		put(DepartmentMgrTPage.class);
		put(RoleMgrTPage.class);
		put(UserMgrTPage.class);
	}
}
