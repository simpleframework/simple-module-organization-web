package net.simpleframework.organization.web.page.mgr.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.html.HtmlConst;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
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
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.Role.ERoleType;
import net.simpleframework.organization.RoleMember;
import net.simpleframework.organization.RoleMember.ERoleMemberType;
import net.simpleframework.organization.role.IRoleHandler;
import net.simpleframework.organization.web.page.mgr.AddMembersPage;
import net.simpleframework.organization.web.page.mgr.OmgrUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleMembersPage extends AbstractTemplatePage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(RoleMembersPage.class, "/role_members.css");

		// 添加成员
		addMemberWindow(pp);

		// 成员列表
		addTablePagerBean(pp);

		// 删除成员
		addDeleteAjaxRequest(pp, "RoleMembersPage_del");

		addAjaxRequest(pp, "RoleMembersPage_primary").setHandlerMethod("doPrimaryRole");

		// 保存规则角色
		addAjaxRequest(pp, "RoleMembersPage_save").setHandlerMethod("doRoleSave");
		// 移动
		addAjaxRequest(pp, "RoleMemberPage_Move").setHandlerMethod("doMove");
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) super
				.addTablePagerBean(pp, "RoleMembersPage_tbl", MemberTable.class).setFilter(false)
				.setSort(false).setPagerBarLayout(EPagerBarLayout.bottom)
				.setContainerId("idRoleMembersPage_tbl");
		tablePager
				.addColumn(
						new TablePagerColumn("memberType", $m("RoleMembersPage.2"), 60)
								.setPropertyClass(ERoleMemberType.class))
				.addColumn(new TablePagerColumn("memberId", $m("RoleMembersPage.3"), 150))
				.addColumn(new TablePagerColumn("deptId", $m("RoleMembersPage.5"), 150))
				.addColumn(TablePagerColumn.BOOL("primaryRole", $m("RoleMembersPage.4")).setWidth(60))
				.addColumn(TablePagerColumn.DESCRIPTION()).addColumn(TablePagerColumn.OPE(70));
		return tablePager;
	}

	protected void addMemberWindow(final PageParameter pp) {
		final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "ajax_addMemberPage",
				AddMembersPage.class);
		addWindowBean(pp, "addMemberWindow", ajaxRequest).setTitle($m("RoleMembersPage.1"))
				.setHeight(340).setWidth(320);
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("mId"), ";");
		_rolemService.delete(ids);
		return new JavascriptForward("$Actions['RoleMembersPage_tbl']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doPrimaryRole(final ComponentParameter cp) {
		_rolemService.setPrimary(_rolemService.getBean(cp.getParameter("mId")));
		return new JavascriptForward("$Actions['RoleMembersPage_tbl']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doRoleSave(final ComponentParameter cp) {
		final Role role = _roleService.getBean(cp.getParameter("roleId"));
		if (role != null) {
			final String ruleValue = cp.getParameter("role_ruleValue");
			if (role.getRoleType() == ERoleType.handle) {
				role.setRuleHandler(ruleValue);
				_roleService.update(new String[] { "rulehandler" }, role);
			} else {
				role.setRuleScript(ruleValue);
				_roleService.update(new String[] { "rulescript" }, role);
			}
			return new JavascriptForward("alert('").append($m("RoleMembersPage.8")).append("');");
		} else {
			return null;
		}
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doMove(final ComponentParameter cp) {
		_rolemService.exchange(TablePagerUtils.getExchangeBeans(cp, _rolemService));
		return new JavascriptForward("$Actions['RoleMembersPage_tbl']();");
	}

	public static ElementList getActionElements(final PageParameter pp) {
		final ElementList el = ElementList.of();
		final Role role = OmgrUtils.getRole(pp);
		final ERoleType rt = role != null ? role.getRoleType() : null;
		if (rt == ERoleType.normal) {
			el.append(LinkButton.of($m("RoleMembersPage.1")).setOnclick(
					"$Actions['addMemberWindow']('roleId=" + role.getId() + "');"));
			el.append(SpanElement.SPACE);
			el.append(LinkButton.deleteBtn().setOnclick(
					"$Actions['RoleMembersPage_tbl'].doAct('RoleMembersPage_del', 'mId');"));
		} else if (rt == ERoleType.handle) {
			el.append(LinkButton.saveBtn().setOnclick(
					"$Actions['RoleMembersPage_save']($Form('#idRoleMgrPage_ajax_roleMember .rule'));"));
		} else if (rt == ERoleType.script) {
			el.append(LinkButton.saveBtn().setOnclick(
					"$Actions['RoleMembersPage_save']($Form('#idRoleMgrPage_ajax_roleMember .rule'));"));
			el.append(SpanElement.SPACE);
			el.add(LinkButton.of($m("RoleMembersPage.9")));
		}
		return el;
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String variable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='RoleMembersPage'>");
		sb.append(" <div class='tb clearfix'>");
		sb.append("  <div class='nav_arrow'>");
		final Role role = OmgrUtils.getRole(pp);
		if (role != null) {
			sb.append(role.getText());
			sb.append(SpanElement.shortText("(" + role.getName() + ")"));
		} else {
			sb.append("#(RoleMembersPage.0)");
		}
		sb.append("  </div>");
		sb.append("  <div class='btn'>").append(getActionElements(pp)).append("</div>");
		sb.append(" </div>");

		final ERoleType rt = role != null ? role.getRoleType() : null;
		if (rt == ERoleType.normal) {
			sb.append("<div id='idRoleMembersPage_tbl'></div>");
		} else {
			sb.append("<div class='rule'>");
			if (role != null) {
				sb.append(InputElement.hidden().setName("roleId").setVal(role.getId()));
			}
			if (rt == ERoleType.handle) {
				sb.append("<div class='t'>#(RoleMembersPage.10)").append(HtmlConst.NBSP)
						.append(IRoleHandler.class.getName()).append("</div>");
				sb.append("<div class='c'>");
				final InputElement ta = InputElement.textarea().setName("role_ruleValue").setRows(1);
				final IRoleHandler rHandler = _roleService.getRoleHandler(role);
				if (rHandler != null) {
					ta.setValue(rHandler.getClass().getName());
				}
				sb.append(ta);
			} else if (rt == ERoleType.script) {
				sb.append("<div class='t'>#(RoleMembersPage.11)</div>");
				sb.append("<div class='c'>");
				sb.append(InputElement.textarea().setName("role_ruleValue").setRows(14)
						.setValue(role.getRuleScript()));
			}
			sb.append("</div></div>");
		}
		sb.append("</div>");
		return sb.toString();
	}

	public static class MemberTable extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Role role = OmgrUtils.getRole(cp);
			if (role != null) {
				cp.addFormParameter("roleId", role.getId());
			}
			final Department dept = _deptService.getBean(cp.getParameter("deptId"));
			if (dept != null) {
				cp.addFormParameter("deptId", dept.getId());
			}
			return _rolemService.queryRoleMembers(role, dept);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final RoleMember rm = (RoleMember) dataObject;
			final ID id = rm.getId();
			final KVMap kv = new KVMap();
			final ERoleMemberType mType = rm.getMemberType();
			kv.put("memberType", mType);
			kv.put("memberId", rm.toString());
			if (mType == ERoleMemberType.user) {
				kv.put("primaryRole", new Checkbox(null, null).setChecked(rm.isPrimaryRole())
						.setOnclick("$Actions['RoleMembersPage_primary']('mId=" + id + "');"));
				kv.put("deptId",
						AccountMgrPageUtils.toDepartmentText(_deptService.getBean(rm.getDeptId())));
			}
			kv.put(TablePagerColumn.DESCRIPTION, rm.getDescription());
			final StringBuilder sb = new StringBuilder();
			sb.append(ButtonElement.deleteBtn().setOnclick(
					"$Actions['RoleMembersPage_del']('mId=" + id + "');"));
			sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
			kv.put(TablePagerColumn.OPE, sb.toString());
			return kv;
		}

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			if (menuItem == null) {
				final MenuItems items = MenuItems.of(MenuItem.itemDelete().setOnclick_act(
						"RoleMembersPage_del", "mId"));
				items.append(MenuItem.sep());
				// 移动菜单
				items.append(MenuItem.TBL_MOVE_UP("RoleMemberPage_Move"));
				items.append(MenuItem.TBL_MOVE_UP2("RoleMemberPage_Move"));
				items.append(MenuItem.TBL_MOVE_DOWN("RoleMemberPage_Move"));
				items.append(MenuItem.TBL_MOVE_DOWN2("RoleMemberPage_Move"));
				return items;
			}
			return null;
		}
	}
}
