package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.bean.Department;
import net.simpleframework.organization.bean.User;

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

	@Override
	protected void addComponentsBean(final PageParameter pp) {
		super.addComponentsBean(pp);
		// 注销
		addAjaxRequest(pp, "UserMgrTPage_logout").setConfirmMessage($m("AccountMgrPage.17"))
				.setHandlerMethod("doLogout");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doLogout(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		_accountService.logout(ids);
		return createTableRefresh();
	}

	public static class UserTbl_Online extends UserTbl {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Department org = getOrg2(cp);
			if (org != null) {
				return _accountService.queryAccounts(org, Account.TYPE_ONLINE);
			}
			return null;
		}

		@Override
		protected String toOpeHTML(final ComponentParameter cp, final User user) {
			final StringBuilder sb = new StringBuilder();
			sb.append(new ButtonElement($m("AccountMgrPage.13"))
					.setOnclick("$Actions['UserMgrTPage_logout']('id=" + user.getId() + "')"));
			sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
			return sb.toString();
		}
	}
}
