package net.simpleframework.organization.web.page2;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EAccountStatus;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserMgr_DelTPage extends UserMgrTPage {

	@Override
	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		return (TablePagerBean) super.addTablePagerBean(pp).setHandlerClass(_UserTbl.class);
	}

	public static class _UserTbl extends UserTbl {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Department org = getOrg(cp);
			if (org != null) {
				cp.addFormParameter("orgId", org.getId());
				return orgContext.getUserService().queryUsers(org, EAccountStatus.delete);
			}
			return null;
		}
	}
}
