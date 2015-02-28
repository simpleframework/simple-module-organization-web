package net.simpleframework.organization.web.page.mgr;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ado.query.IteratorDataQuery;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.PageRequestResponse.IVal;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.ERoleMemberType;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleMemberService;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleMember;
import net.simpleframework.organization.User;
import net.simpleframework.organization.web.component.roleselect.RoleSelectBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserRolesPage extends OneTableTemplatePage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		final TablePagerBean tablePager = addTablePagerBean(pp, "UserRolesPage_tbl",
				UserRolesTbl.class).setShowFilterBar(false).setSort(false);
		tablePager
				.addColumn(
						new TablePagerColumn("roletext", $m("RoleMgrTPage.0"), 180)
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("rolename", $m("RoleMgrTPage.1"), 120)
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("roletype", $m("RoleMgrTPage.2"), 90)
								.setTextAlign(ETextAlign.left)).addColumn(TablePagerColumn.DESCRIPTION())
				.addColumn(TablePagerColumn.OPE().setWidth(80));

		// 角色选取
		final User user = getUser(pp);
		addComponentBean(pp, "UserRolesPage_roleSelect", RoleSelectBean.class)
				.setClearAction("false").setJsSelectCallback(
						"$Actions['UserRolesPage_roleSelect_OK']('accountId=" + user.getId()
								+ "&roleId=' +　selects[0].id);");
		// 角色选取确认
		addAjaxRequest(pp, "UserRolesPage_roleSelect_OK").setHandlerMethod("doRoleSelected");

		// 角色删除
		addAjaxRequest(pp, "UserRolesPage_del").setConfirmMessage($m("Confirm.Delete"))
				.setHandlerMethod("doMemberDel");
	}

	public IForward doRoleSelected(final ComponentParameter cp) {
		final User user = getUser(cp);
		final Role r = orgContext.getRoleService().getBean(cp.getParameter("roleId"));
		final IRoleMemberService rmService = orgContext.getRoleMemberService();
		final RoleMember rm = rmService.createBean();
		rm.setRoleId(r.getId());
		rm.setMemberType(ERoleMemberType.user);
		rm.setMemberId(user.getId());
		rm.setDeptId(user.getDepartmentId());
		rmService.insert(rm);
		return new JavascriptForward(
				"$Actions['UserRolesPage_roleSelect'].close(); $Actions['UserRolesPage_tbl']();");
	}

	public IForward doMemberDel(final ComponentParameter cp) {
		final User user = getUser(cp);
		final Role r = orgContext.getRoleService().getBean(cp.getParameter("roleId"));
		final IRoleMemberService rmService = orgContext.getRoleMemberService();
		final RoleMember rm = rmService.getRoleMember(r, ERoleMemberType.user, user.getId());
		if (rm != null) {
			rmService.delete(rm.getId());
		}
		return new JavascriptForward("$Actions['UserRolesPage_tbl']();");
	}

	private static User getUser(final PageParameter pp) {
		return pp.getCache("_user", new IVal<User>() {
			@Override
			public User get() {
				return orgContext.getUserService().getBean(pp.getParameter("accountId"));
			}
		});
	}

	protected Department getOrg(final PageParameter pp) {
		return null;
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final Department org = getOrg(pp);
		return ElementList.of(LinkButton.addBtn().setOnclick(
				"$Actions['UserRolesPage_roleSelect']("
						+ (org != null ? "'orgId=" + org.getId() + "'" : "") + ");"));
	}

	public static class UserRolesTbl extends AbstractDbTablePagerHandler {

		private final IRoleService rService = orgContext.getRoleService();

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final User user = getUser(cp);
			if (user != null) {
				cp.addFormParameter("accountId", user.getId());
				return new IteratorDataQuery<Role>(rService.roles(user, new KVMap().add("inOrg", true)));
			}
			return null;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final KVMap kv = new KVMap();
			final Role r = (Role) dataObject;
			kv.add("rolename", rService.toUniqueName(r));
			kv.add("roletext", r.getText());
			kv.add("roletype", r.getRoleType());

			final StringBuilder sb = new StringBuilder();
			final User user = getUser(cp);
			sb.append(ButtonElement.deleteBtn().setOnclick(
					"$Actions['UserRolesPage_del']('accountId=" + user.getId() + "&roleId=" + r.getId()
							+ "');"));
			kv.add(TablePagerColumn.OPE, sb.toString());
			return kv;
		}
	}
}
