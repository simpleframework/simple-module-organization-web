package net.simpleframework.organization.web.page.mgr.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.Icon;
import net.simpleframework.mvc.common.element.LabelElement;
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
import net.simpleframework.mvc.component.ui.pager.TablePagerUtils;
import net.simpleframework.mvc.template.TemplateUtils;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;
import net.simpleframework.mvc.template.t1.ext.LCTemplateTablePagerHandler;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EAccountStatus;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IUserService;
import net.simpleframework.organization.User;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationLogRef;
import net.simpleframework.organization.web.page.attri.AccountStatPage;
import net.simpleframework.organization.web.page.mgr.AccountEditPage;
import net.simpleframework.organization.web.page.mgr.DepartmentCategory;
import net.simpleframework.organization.web.page.mgr.UserRolesPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/org/account/mgr")
public class AccountMgrPage extends CategoryTableLCTemplatePage implements
		IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		pp.addImportCSS(AccountMgrPage.class, "/account_mgr.css");

		addCategoryBean(pp, DepartmentCategory.class);

		// 账号列表
		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp, AccountList.class)
				.setPagerBarLayout(EPagerBarLayout.bottom);
		tablePager
				.addColumn(
						new TablePagerColumn("u.departmentId", $m("AccountMgrPage.5")).setFilter(false))
				.addColumn(AccountMgrPageUtils.TC_NAME()).addColumn(AccountMgrPageUtils.TC_TEXT())
				.addColumn(AccountMgrPageUtils.TC_EMAIL()).addColumn(AccountMgrPageUtils.TC_MOBILE())
				.addColumn(AccountMgrPageUtils.TC_LASTLOGINDATE())
				.addColumn(AccountMgrPageUtils.TC_STATUS())
				.addColumn(TablePagerColumn.OPE().setWidth(125));

		// 添加账号
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "AccountMgrPage_editPage",
				AccountEditPage.class);
		addWindowBean(pp, "AccountMgrPage_edit", ajaxRequest).setTitle($m("AccountMgrPage.8"))
				.setHeight(500).setWidth(620);
		// 帐号信息
		ajaxRequest = addAjaxRequest(pp, "AccountMgrPage_accountPage", AccountStatPage.class);
		addWindowBean(pp, "AccountMgrPage_accountWin", ajaxRequest).setTitle($m("AccountMgrPage.18"))
				.setHeight(450).setWidth(380);

		// 添加角色
		ajaxRequest = addAjaxRequest(pp, "AccountMgrPage_rolePage", UserRolesPage.class);
		addWindowBean(pp, "AccountMgrPage_roleWin", ajaxRequest).setTitle($m("AccountMgrPage.19"))
				.setHeight(480).setWidth(800);

		// 删除账号
		addDeleteAjaxRequest(pp, "AccountMgrPage_delete");
		// 取消删除
		addAjaxRequest(pp, "AccountMgrPage_undelete").setConfirmMessage($m("AccountMgrPage.14"))
				.setHandlerMethod("doUndeleteAccount");

		// 锁定
		addAjaxRequest(pp, "AccountMgrPage_lock").setConfirmMessage($m("AccountMgrPage.15"))
				.setHandlerMethod("doLockAccount");
		// 解锁
		addAjaxRequest(pp, "AccountMgrPage_unlock").setConfirmMessage($m("AccountMgrPage.16"))
				.setHandlerMethod("doUnLockAccount");

		// 注销
		addAjaxRequest(pp, "AccountMgrPage_logout").setConfirmMessage($m("AccountMgrPage.17"))
				.setHandlerMethod("doLogout");
		// 移动
		addAjaxRequest(pp, "AccountMgrPage_Move").setHandlerMethod("doMove");

		// 日志
		final IModuleRef ref = ((IOrganizationWebContext) orgContext).getLogRef();
		if (ref != null) {
			((OrganizationLogRef) ref).addLogComponent(pp, AccountMgrPage.class);
		}
	}

	@Override
	public String getRole(final PageParameter pp) {
		return orgContext.getModule().getManagerRole();
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doLockAccount(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		orgContext.getAccountService().lock(ids);
		return createTableRefresh();
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doUnLockAccount(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		orgContext.getAccountService().unlock(ids);
		return createTableRefresh();
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		orgContext.getAccountService().delete(ids);
		return createTableRefresh();
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doLogout(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		orgContext.getAccountService().logout(ids);
		return createTableRefresh();
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doUndeleteAccount(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		orgContext.getAccountService().undelete(ids);
		return createTableRefresh();
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doMove(final ComponentParameter cp) {
		final IUserService service = orgContext.getUserService();
		final User item = service.getBean(cp.getParameter(TablePagerUtils.PARAM_MOVE_ROWID));
		final User item2 = service.getBean(cp.getParameter(TablePagerUtils.PARAM_MOVE_ROWID2));
		if (item != null && item2 != null) {
			service.exchange(item, item2,
					Convert.toBool(cp.getParameter(TablePagerUtils.PARAM_MOVE_UP)));
		}
		return createTableRefresh();
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final ElementList eles = ElementList.of(new LinkElement($m("AccountMgrPage." + Account.ALL))
				.setOnclick(createTableRefresh("deptId=&type=" + Account.ALL).toString()));
		final Object s = getSelectedTreeNode(pp);
		if (s instanceof Department) {
			eles.append(SpanElement.NAV).append(new LabelElement(s));
		} else {
			final int type = Convert.toInt(s, Account.ALL);
			if (type != Account.ALL) {
				eles.append(SpanElement.NAV);
				if (type >= Account.STATE_DELETE_ID && type <= Account.STATE_NORMAL_ID) {
					eles.append(new LabelElement(EAccountStatus.values()[Account.STATE_NORMAL_ID - type]));
				} else {
					eles.append(new LabelElement($m("AccountMgrPage." + type)));
				}
			}
		}
		return eles;
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final Object s = getSelectedTreeNode(pp);
		final LinkButton add = new LinkButton($m("AccountMgrPage.10"));
		final ElementList btns = ElementList.of(add);
		final int type = Convert.toInt(s);
		LinkButton del;
		if (type == Account.ONLINE_ID) {
			del = act_btn("AccountMgrPage_logout", $m("AccountMgrPage.13")).setIconClass(Icon.user);
		} else {
			del = delete_btn("AccountMgrPage_delete");
		}
		btns.append(del);
		if (type != Account.STATE_REGISTRATION_ID && type != Account.STATE_DELETE_ID) {
			if (type == Account.STATE_LOCKED_ID) {
				btns.append(
						SpanElement.SPACE,
						act_btn("AccountMgrPage_unlock", $m("AccountMgrPage.12")).setIconClass(
								"icon-unlock"));
			} else {
				btns.append(
						SpanElement.SPACE,
						act_btn("AccountMgrPage_lock", EAccountStatus.locked.toString()).setIconClass(
								Icon.lock));
			}
		}

		if (s instanceof Department) {
			add.setOnclick("$Actions['AccountMgrPage_edit']('deptId=" + ((Department) s).getId()
					+ "');");
		} else {
			add.setOnclick("$Actions['AccountMgrPage_edit']();");
			if (type == Account.STATE_DELETE_ID) {
				btns.append(
						SpanElement.SPACE,
						act_btn("AccountMgrPage_undelete", $m("AccountMgrPage.11")).setIconClass(
								Icon.share_alt));
			}
		}
		return btns;
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		return super.getNavigationBar(pp).append(new SpanElement($m("AccountMgrPage.0")));
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		return TabButtons.of(new TabButton($m("AccountMgrPage.0"), url(AccountMgrPage.class)),
				new TabButton($m("RoleMgrPage.0"), url(RoleMgrPage.class)));
	}

	private static Object getSelectedTreeNode(final PageParameter pp) {
		return pp.getRequestAttr("select_category");
	}

	public static class AccountList extends LCTemplateTablePagerHandler {

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			if (menuItem != null) {
				return null;
			}

			final MenuItems items = MenuItems.of();
			items.add(MenuItem.of($m("AccountMgrPage.18")).setOnclick_act("AccountMgrPage_accountWin",
					"accountId"));

			final int type = Convert.toInt(getSelectedTreeNode(cp));
			if (type != Account.STATE_DELETE_ID) {
				items.add(MenuItem.sep());
				items.add(MenuItem.of($m("AccountMgrPage.22")).setOnclick_act("AccountMgrPage_roleWin",
						"accountId"));
				items.add(MenuItem.sep());
				items.add(MenuItem.itemEdit().setOnclick_act("AccountMgrPage_edit", "accountId"));
			}

			if (type != Account.ONLINE_ID) {
				items.add(MenuItem.sep());
				MenuItem itemDelete;
				if (type == Account.STATE_DELETE_ID) {
					itemDelete = MenuItem.of($m("AccountMgrPage.21"));
				} else {
					itemDelete = MenuItem.itemDelete();
				}
				items.add(itemDelete.setOnclick_act("AccountMgrPage_delete", "id"));
			}

			items.add(MenuItem.sep());
			items.add(MenuItem.itemLog().setOnclick_act("AccountMgrPage_logWin", "beanId"));

			if (type != Account.STATE_DELETE_ID) {
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
			}
			return items;
		}

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			IDataQuery<?> dq;
			final String deptId = cp.getParameter("deptId");
			Department dept;
			if (StringUtils.hasText(deptId)
					&& (dept = orgContext.getDepartmentService().getBean(deptId)) != null) {
				cp.setRequestAttr("select_category", dept);
				dq = orgContext.getUserService().queryUsers(dept);
			} else {
				final int type = Convert.toInt(cp.getParameter("type"), Account.ALL);
				cp.setRequestAttr("select_category", type);
				dq = orgContext.getAccountService().queryAccounts(null, type);
			}
			return dq;
		}

		@Override
		public Map<String, Object> getFormParameters(final ComponentParameter cp) {
			final Map<String, Object> m = super.getFormParameters(cp);
			final Object s = getSelectedTreeNode(cp);
			if (s instanceof Department) {
				m.put("deptId", ((Department) s).getId());
			} else {
				m.put("type", s);
			}
			return m;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			Account account;
			User user;
			ID id;
			if (dataObject instanceof Account) {
				account = (Account) dataObject;
				user = orgContext.getAccountService().getUser(id = account.getId());
			} else {
				user = (User) dataObject;
				account = orgContext.getUserService().getAccount(id = user.getId());
			}

			final KVMap kv = new KVMap();
			kv.add("name", account.getName());
			kv.add("lastLoginDate", account.getLastLoginDate());
			kv.add("status", account.getStatus());

			kv.add("u.departmentId", AccountMgrPageUtils.toDepartmentText(user.getDepartmentId()));

			kv.add("u.text", TemplateUtils.toIconUser(cp, user.getId()));
			final String email = user.getEmail();
			kv.add("u.email", new LinkElement(email).setHref("mailto:" + email));
			kv.add("u.mobile", user.getMobile());

			final StringBuilder sb = new StringBuilder();
			final EAccountStatus status = account.getStatus();
			final int type = Convert.toInt(getSelectedTreeNode(cp));
			if (type == Account.ONLINE_ID) {
				sb.append(new ButtonElement($m("AccountMgrPage.13"))
						.setOnclick("$Actions['AccountMgrPage_logout']('id=" + id + "');"));
			} else if (status == EAccountStatus.locked) {
				sb.append(new ButtonElement($m("AccountMgrPage.12"))
						.setOnclick("$Actions['AccountMgrPage_unlock']('id=" + id + "');"));
			} else if (status == EAccountStatus.delete) {
				sb.append(new ButtonElement($m("AccountMgrPage.11"))
						.setOnclick("$Actions['AccountMgrPage_undelete']('id=" + id + "');"));
			} else {
				sb.append(ButtonElement.editBtn().setOnclick(
						"$Actions['AccountMgrPage_edit']('accountId=" + id + "');"));
			}
			sb.append(SpanElement.SPACE).append(
					ButtonElement.logBtn().setOnclick(
							"$Actions['AccountMgrPage_logWin']('beanId=" + id + "');"));
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			kv.add(TablePagerColumn.OPE, sb.toString());
			return kv;
		}
	}
}
