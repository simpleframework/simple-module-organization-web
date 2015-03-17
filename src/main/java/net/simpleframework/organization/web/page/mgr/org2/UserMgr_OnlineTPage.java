package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserMgr_OnlineTPage extends UserMgrTPage {

	@Override
	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		return (TablePagerBean) super.addTablePagerBean(pp).setHandlerClass(UserTbl_Online.class);
	}

	public static class UserTbl_Online extends UserTbl {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Department org = getOrg2(cp);
			if (org != null) {
				cp.addFormParameter("orgId", org.getId());
				return orgContext.getAccountService().queryAccounts(getOrg2(cp), Account.ONLINE_ID);
			}
			return null;
		}

		@Override
		protected String toOpeHTML(final ComponentParameter cp, final User user) {
			final StringBuilder sb = new StringBuilder();
			sb.append(new ButtonElement($m("AccountMgrPage.13")));
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			return sb.toString();
		}
	}
}
