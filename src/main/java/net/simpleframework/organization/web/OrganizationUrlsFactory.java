package net.simpleframework.organization.web;

import net.simpleframework.mvc.common.UrlsCache;
import net.simpleframework.organization.web.page.mgr.org2.DepartmentMgrTPage;
import net.simpleframework.organization.web.page.mgr.org2.RoleMgrTPage;
import net.simpleframework.organization.web.page.mgr.org2.RoleMgr_MembersTPage;
import net.simpleframework.organization.web.page.mgr.org2.UserMgrTPage;
import net.simpleframework.organization.web.page.mgr.org2.UserMgr_DelTPage;
import net.simpleframework.organization.web.page.mgr.org2.UserMgr_OnlineTPage;

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
		put(RoleMgr_MembersTPage.class);
		put(UserMgrTPage.class);
		put(UserMgr_DelTPage.class);
		put(UserMgr_OnlineTPage.class);
	}
}
