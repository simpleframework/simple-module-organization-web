package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
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
public class UserMgr_DelTPage extends UserMgrTPage {
	@Override
	protected void addComponentsBean(final PageParameter pp) {
		// 取消删除
		addAjaxRequest(pp, "UserMgr_DelTPage_undelete").setConfirmMessage($m("AccountMgrPage.14"))
				.setHandlerMethod("doUndeleteAccount");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doUndeleteAccount(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		_accountService.undelete(ids);
		return new JavascriptForward("$Actions['UserMgrTPage_tbl']();");
	}

	@Override
	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		return (TablePagerBean) super.addTablePagerBean(pp).setHandlerClass(UserTbl_Del.class);
	}

	@Override
	protected ElementList getOpeActions(final PageParameter pp) {
		return ElementList.of(LinkButton.of($m("AccountMgrPage.21")).setOnclick(
				"$Actions['UserMgrTPage_tbl'].doAct('UserMgrTPage_delete');"));
	}

	public static class UserTbl_Del extends UserTbl {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Department org = getOrg2(cp);
			if (org != null) {
				return _userService.queryUsers(org, Account.TYPE_STATE_DELETE);
			}
			return null;
		}

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			if (menuItem != null) {
				return null;
			}
			final MenuItems items = MenuItems.of();
			items.add(MenuItem.of($m("AccountMgrPage.18")).setOnclick_act("UserMgrTPage_accountWin",
					"accountId"));
			items.add(MenuItem.sep());
			items.add(MenuItem.of($m("AccountMgrPage.21")).setOnclick_act("UserMgrTPage_delete", "id"));
			items.add(MenuItem.sep());
			items.add(MenuItem.itemLog().setOnclick_act("UserMgrTPage_logWin", "beanId"));
			return items;
		}

		@Override
		protected String toOpeHTML(final ComponentParameter cp, final User user) {
			final StringBuilder sb = new StringBuilder();
			sb.append(new ButtonElement($m("AccountMgrPage.11"))
					.setOnclick("$Actions['UserMgr_DelTPage_undelete']('id=" + user.getId() + "');"));
			sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
			return sb.toString();
		}
	}
}
