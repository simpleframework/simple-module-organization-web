package net.simpleframework.organization.web.page.mgr;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ado.query.IteratorDataQuery;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleService.RoleM;
import net.simpleframework.organization.bean.Department;
import net.simpleframework.organization.bean.Role;
import net.simpleframework.organization.bean.RoleMember;
import net.simpleframework.organization.bean.RoleMember.ERoleMemberType;
import net.simpleframework.organization.bean.User;
import net.simpleframework.organization.web.component.roleselect.RoleSelectBean;
import net.simpleframework.organization.web.page.mgr.t1.AccountMgrPageUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserRolesPage extends OneTableTemplatePage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		final TablePagerBean tablePager = addTablePagerBean(pp, "UserRolesPage_tbl",
				UserRolesTbl.class).setFilter(false).setSort(false);
		tablePager.addColumn(new TablePagerColumn("roletext", $m("RoleMgrTPage.0"), 180))
				.addColumn(new TablePagerColumn("deptId", $m("RoleMgrTPage.6"), 120))
				.addColumn(new TablePagerColumn("roletype", $m("RoleMembersPage.2"), 80))
				.addColumn(TablePagerColumn.DESCRIPTION()).addColumn(TablePagerColumn.OPE(75));

		// 角色选取
		final User user = getUser(pp);
		addComponentBean(pp, "UserRolesPage_roleSelect", RoleSelectBean.class).setClearAction("false")
				.setJsSelectCallback("$Actions['UserRolesPage_roleSelect_OK']('accountId="
						+ user.getId() + "&roleId=' +　selects[0].id);");
		// 角色选取确认
		addAjaxRequest(pp, "UserRolesPage_roleSelect_OK").setHandlerMethod("doRoleSelected");

		// 角色删除
		addAjaxRequest(pp, "UserRolesPage_del").setConfirmMessage($m("Confirm.Delete"))
				.setHandlerMethod("doMemberDel");
	}

	public IForward doRoleSelected(final ComponentParameter cp) {
		final User user = getUser(cp);
		final Role r = _roleService.getBean(cp.getParameter("roleId"));
		final RoleMember rm = _rolemService.createBean();
		rm.setRoleId(r.getId());
		rm.setMemberType(ERoleMemberType.user);
		rm.setMemberId(user.getId());
		rm.setDeptId(user.getDepartmentId());
		_rolemService.insert(rm);
		return new JavascriptForward(
				"$Actions['UserRolesPage_roleSelect'].close(); $Actions['UserRolesPage_tbl']();");
	}

	public IForward doMemberDel(final ComponentParameter cp) {
		final RoleMember rm = _rolemService.getBean(cp.getParameter("mid"));
		if (rm != null) {
			_rolemService.delete(rm.getId());
		}
		return new JavascriptForward("$Actions['UserRolesPage_tbl']();");
	}

	private static User getUser(final PageParameter pp) {
		return pp.getRequestCache("_user", new CacheV<User>() {
			@Override
			public User get() {
				return _userService.getBean(pp.getParameter("accountId"));
			}
		});
	}

	protected Department getOrg(final PageParameter pp) {
		return null;
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final Department org = getOrg(pp);
		return ElementList.of(LinkButton.addBtn().setOnclick("$Actions['UserRolesPage_roleSelect']("
				+ (org != null ? "'orgId=" + org.getId() + "'" : "") + ");"));
	}

	public static class UserRolesTbl extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final User user = getUser(cp);
			if (user != null) {
				cp.addFormParameter("accountId", user.getId());
				return new IteratorDataQuery<RoleM>(
						_roleService.roles(user, new KVMap().add("inOrg", true)));
			}
			return null;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp,
				final Object dataObject) {
			final KVMap kv = new KVMap();
			final RoleM rolem = (RoleM) dataObject;
			final Role r = rolem.role;
			kv.add("roletext", new StringBuilder(r.getText()).append("<br>")
					.append(SpanElement.color777(_roleService.toUniqueName(r)).setItalic(true)));
			kv.add("roletype", rolem.rm.getMemberType());
			kv.put("deptId",
					AccountMgrPageUtils.toDepartmentText(_deptService.getBean(rolem.rm.getDeptId())));
			kv.add(TablePagerColumn.OPE, ButtonElement.deleteBtn()
					.setOnclick("$Actions['UserRolesPage_del']('mid=" + rolem.rm.getId() + "');"));
			return kv;
		}
	}
}
