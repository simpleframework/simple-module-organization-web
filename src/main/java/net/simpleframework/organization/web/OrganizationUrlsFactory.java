package net.simpleframework.organization.web;

import net.simpleframework.mvc.common.UrlsCache;
import net.simpleframework.organization.web.page.attri.AbstractAttriTPage.AccountPasswordTPage;
import net.simpleframework.organization.web.page.attri.AbstractAttriTPage.AccountStatTPage;
import net.simpleframework.organization.web.page.attri.AbstractAttriTPage.PhotoTPage;
import net.simpleframework.organization.web.page.attri.AbstractAttriTPage.UserAttriTPage;
import net.simpleframework.organization.web.page.attri.t2.AbstractAttriPage.AccountPasswordPageT2;
import net.simpleframework.organization.web.page.attri.t2.AbstractAttriPage.AccountStatPageT2;
import net.simpleframework.organization.web.page.attri.t2.AbstractAttriPage.PhotoPageT2;
import net.simpleframework.organization.web.page.attri.t2.AbstractAttriPage.UserAttriPageT2;
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

		put(UserAttriTPage.class, UserAttriPageT2.class);
		put(AccountStatTPage.class, AccountStatPageT2.class);
		put(AccountPasswordTPage.class, AccountPasswordPageT2.class);
		put(PhotoTPage.class, PhotoPageT2.class);
	}
}
