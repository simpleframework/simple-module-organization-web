package net.simpleframework.organization.web.page2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.TemplateUtils;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.User;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationUrlsFactory;
import net.simpleframework.organization.web.component.deptselect.DeptSelectBean;
import net.simpleframework.organization.web.page.attri.AccountStatPage;
import net.simpleframework.organization.web.page.mgr.AccountEditPage;
import net.simpleframework.organization.web.page.mgr.UserRolesPage;
import net.simpleframework.organization.web.page.mgr.t1.AccountMgrPageUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserMgrTPage extends AbstractMgrTPage {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		addTablePagerBean(pp);

		// 添加账号
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "UserMgrTPage_editPage",
				_AccountEditPage.class);
		addWindowBean(pp, "UserMgrTPage_edit", ajaxRequest).setTitle($m("AccountMgrPage.8"))
				.setHeight(500).setWidth(620);
		// 帐号信息
		ajaxRequest = addAjaxRequest(pp, "UserMgrTPage_accountPage", AccountStatPage.class);
		addWindowBean(pp, "UserMgrTPage_accountWin", ajaxRequest).setTitle($m("AccountMgrPage.18"))
				.setHeight(450).setWidth(380);

		// 添加角色
		ajaxRequest = addAjaxRequest(pp, "UserMgrTPage_rolePage", _UserRolesPage.class);
		addWindowBean(pp, "UserMgrTPage_roleWin", ajaxRequest).setTitle($m("AccountMgrPage.19"))
				.setHeight(480).setWidth(800);

		// 删除账号
		addDeleteAjaxRequest(pp, "UserMgrTPage_delete");

		// 部门选取
		addComponentBean(pp, "UserMgrTPage_deptSelect", DeptSelectBean.class)
				.setMultiple(false)
				.setClearAction("false")
				.setJsSelectCallback(
						"$Actions['UserMgrTPage_tbl']('filter_cur_col=u.departmentId&filter=%3D;' + selects[0].id);return true;");
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final Department org = getOrg(pp);
		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp, "UserMgrTPage_tbl")
				.setPagerBarLayout(EPagerBarLayout.bottom).setPageItems(30)
				.setContainerId("idUserMgrTPage_tbl").setHandlerClass(UserTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("u.departmentId", $m("AccountMgrPage.5")) {
							@Override
							public String getFilterVal(final String val) {
								if (val == null) {
									return null;
								}
								final PermissionDept dept = pp.getPermission().getDept(ID.of(val));
								return dept.getId() != null ? dept.getText() : val;
							}
						}.setFilterAdvClick(
								"$Actions['UserMgrTPage_deptSelect']("
										+ (org == null ? "" : "'orgId=" + org.getId() + "'") + ");")
								.setTextAlign(ETextAlign.left)).addColumn(AccountMgrPageUtils.TC_NAME())
				.addColumn(AccountMgrPageUtils.TC_TEXT()).addColumn(AccountMgrPageUtils.TC_EMAIL())
				.addColumn(AccountMgrPageUtils.TC_MOBILE())
				.addColumn(AccountMgrPageUtils.TC_LASTLOGINDATE())
				.addColumn(AccountMgrPageUtils.TC_STATUS())
				.addColumn(TablePagerColumn.OPE().setWidth(122));
		return tablePager;
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		orgContext.getAccountService().delete(ids);
		return new JavascriptForward("$Actions['UserMgrTPage_tbl']();");
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='tbar UserMgrTPage_tbar'>");
		sb.append(ElementList.of(LinkButton.addBtn().setOnclick("$Actions['UserMgrTPage_edit']();"),
				SpanElement.SPACE, LinkButton.deleteBtn()));

		String params = null;
		final String orgid = pp.getParameter("orgId");
		if (StringUtils.hasText(orgid)) {
			params = "orgId=" + orgid;
		}
		final OrganizationUrlsFactory urlsFactory = ((IOrganizationWebContext) orgContext)
				.getUrlsFactory();
		sb.append(TabButtons.of(
				new TabButton($m("UserMgrTPage.1")).setHref(urlsFactory.getUrl(pp, UserMgrTPage.class,
						params)),
				new TabButton($m("UserMgrTPage.2")).setHref(urlsFactory.getUrl(pp,
						UserMgr_DelTPage.class, params))).toString(pp));
		sb.append("</div>");
		sb.append("<div id='idUserMgrTPage_tbl'></div>");
		return sb.toString();
	}

	public static class UserTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Department org = getOrg(cp);
			if (org != null) {
				cp.addFormParameter("orgId", org.getId());
				return orgContext.getUserService().queryUsers(org);
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
			items.add(MenuItem.itemEdit().setOnclick_act("UserMgrTPage_edit", "accountId"));
			items.add(MenuItem.itemDelete().setOnclick_act("UserMgrTPage_delete", "id"));
			items.add(MenuItem.sep());
			items.add(MenuItem.itemLog().setOnclick_act("AccountMgrPage_logWin", "beanId"));
			items.add(MenuItem.sep());
			items.append(MenuItem
					.of($m("Menu.move"))
					.addChild(
							MenuItem.of($m("Menu.up"), MenuItem.ICON_UP,
									"$pager_action(item).move(true, 'AccountMgrPage_Move');"))
					.addChild(
							MenuItem.of($m("Menu.up2"), MenuItem.ICON_UP2,
									"$pager_action(item).move2(true, 'AccountMgrPage_Move');"))
					.addChild(
							MenuItem.of($m("Menu.down"), MenuItem.ICON_DOWN,
									"$pager_action(item).move(false, 'AccountMgrPage_Move');"))
					.addChild(
							MenuItem.of($m("Menu.down2"), MenuItem.ICON_DOWN2,
									"$pager_action(item).move2(false, 'AccountMgrPage_Move');")));
			return items;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final KVMap data = new KVMap();
			final User user = (User) dataObject;
			final Account account = orgContext.getUserService().getAccount(user.getId());
			data.add("name", account.getName());
			data.add("u.text", TemplateUtils.toIconUser(cp, user.getId()));

			data.add("lastLoginDate", account.getLastLoginDate()).add("status", account.getStatus());

			final String email = user.getEmail();
			data.add("u.email", new LinkElement(email).setHref("mailto:" + email));
			data.add("u.mobile", user.getMobile());

			data.add("u.departmentId", AccountMgrPageUtils.toDepartmentText(user.getDepartmentId()));

			final Object id = user.getId();
			final StringBuilder sb = new StringBuilder();
			sb.append(
					new ButtonElement($m("UserMgrTPage.0"))
							.setOnclick("$Actions['UserMgrTPage_roleWin']('accountId=" + id + "');"))
					.append(SpanElement.SPACE)
					.append(
							ButtonElement.editBtn().setOnclick(
									"$Actions['UserMgrTPage_edit']('accountId=" + id + "');"));
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			data.add(TablePagerColumn.OPE, sb.toString());
			return data;
		}
	}

	public static class _AccountEditPage extends AccountEditPage {
		@Override
		public String getRole(final PageParameter pp) {
			return IOrganizationContext.ROLE_ORGANIZATION_MANAGER;
		}

		@Override
		protected Department getOrg(final PageParameter pp) {
			return AbstractMgrTPage.getOrg(pp);
		}

		@Override
		protected JavascriptForward toJavascriptForward(final PageParameter pp) {
			final JavascriptForward js = new JavascriptForward("$Actions['UserMgrTPage_tbl']();");
			if (Convert.toBool(pp.getParameter(OPT_NEXT))) {
				js.append("$('").append(getFormSelector()).append("').down('form').reset();");
				js.append("$('ae_accountName').focus();");
			} else {
				js.append("$Actions['UserMgrTPage_edit'].close();");
			}
			return js;
		}
	}

	public static class _UserRolesPage extends UserRolesPage {
	}
}