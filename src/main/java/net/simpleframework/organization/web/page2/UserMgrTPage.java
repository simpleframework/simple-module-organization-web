package net.simpleframework.organization.web.page2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.tree.AbstractTreeHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.template.TemplateUtils;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.User;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationUrlsFactory;
import net.simpleframework.organization.web.page.mgr.AccountEditPage;
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

		// addComponentBean(pp, "UserMgrTPage_dept",
		// TreeBean.class).setContainerId(
		// "idUserMgrTPage_dept").setHandlerClass(DepartmentTree.class);
		addTablePagerBean(pp);

		AccountMgrPageUtils.addAccountComponents(pp, "UserMgrTPage");
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp, "UserMgrTPage_tbl")
				.setPagerBarLayout(EPagerBarLayout.bottom).setPageItems(30)
				.setContainerId("idUserMgrTPage_tbl").setHandlerClass(UserTbl.class);
		AccountMgrPageUtils.addAccountTblCols(tablePager);
		return tablePager;
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
				new TabButton("所有用户").setHref(urlsFactory.getUrl(pp, UserMgrTPage.class, params)),
				new TabButton("已删除用户").setHref(urlsFactory.getUrl(pp, UserMgr_DelTPage.class, params)))
				.toString(pp));
		sb.append("</div>");
		sb.append("<div id='idUserMgrTPage_tbl'></div>");
		return sb.toString();
	}

	public static class DepartmentTree extends AbstractTreeHandler {
		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			final TreeBean treeBean = (TreeBean) cp.componentBean;
			final IDepartmentService dService = orgContext.getDepartmentService();
			final TreeNodes nodes = TreeNodes.of();
			if (parent == null) {
				final Department org = getOrg(cp);
				if (org != null) {
					final IDataQuery<Department> dq = dService.queryChildren(org,
							EDepartmentType.department);
					Department dept;
					while ((dept = dq.next()) != null) {
						final TreeNode node = new TreeNode(treeBean, parent, dept);
						nodes.add(node);
					}
				}
			} else {
				final Department _dept = (Department) parent.getDataObject();
				final IDataQuery<Department> dq = dService.queryChildren(_dept,
						EDepartmentType.department);
				Department dept;
				while ((dept = dq.next()) != null) {
					final TreeNode node = new TreeNode(treeBean, parent, dept);
					nodes.add(node);
				}
			}
			return nodes;
		}
	}

	public static class UserTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Department org = getOrg(cp);
			return org != null ? orgContext.getUserService().queryUsers(org) : null;
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
			items.add(MenuItem.itemEdit().setOnclick_act("AccountMgrPage_edit", "accountId"));
			items.add(MenuItem.itemDelete().setOnclick_act("AccountMgrPage_delete", "id"));
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

			ID deptId;
			if ((deptId = user.getDepartmentId()) != null) {
				final Department dept = orgContext.getDepartmentService().getBean(deptId);
				if (dept != null) {
					data.add("u.departmentId", dept.getText());
				}
			}

			final StringBuilder sb = new StringBuilder();
			sb.append(new ButtonElement($m("UserMgrTPage.0")))
					.append(SpanElement.SPACE)
					.append(
							ButtonElement.editBtn().setOnclick(
									"$Actions['UserMgrTPage_edit']('accountId=" + user.getId() + "');"));
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

		@Override
		protected Department getOrg(final PageParameter pp) {
			return AbstractMgrTPage.getOrg(pp);
		}
	}
}