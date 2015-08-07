package net.simpleframework.organization.web.page.mgr.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.html.HtmlConst;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.Checkbox;
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
import net.simpleframework.organization.ERoleMemberType;
import net.simpleframework.organization.ERoleType;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleHandler;
import net.simpleframework.organization.IRoleMemberService;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleMember;
import net.simpleframework.organization.User;
import net.simpleframework.organization.web.page.mgr.AddMembersPage;

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
		final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "ajax_addMemberPage",
				getAddMembersPageClass());
		addWindowBean(pp, "addMemberWindow", ajaxRequest).setTitle($m("RoleMembersPage.1"))
				.setHeight(340).setWidth(320);

		// 成员列表
		addTablePagerBean(pp);

		// 删除成员
		addDeleteAjaxRequest(pp, "ajax_deleteMember");

		addAjaxRequest(pp, "ajax_editPrimaryRole").setHandlerMethod("doPrimaryRole");

		// 保存规则角色
		addAjaxRequest(pp, "ajax_roleSave").setHandlerMethod("doRoleSave");
		// 移动
		addAjaxRequest(pp, "RoleMemberPage_Move").setHandlerMethod("doMove");
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp, "RoleMemberPage_tbl",
				TablePagerBean.class).setShowFilterBar(false).setShowLineNo(true)
				.setPagerBarLayout(EPagerBarLayout.none).setContainerId("idMemberTable")
				.setHandlerClass(MemberTable.class);
		tablePager
				.addColumn(
						new TablePagerColumn("memberType", $m("RoleMembersPage.2"), 65).setSort(false)
								.setPropertyClass(ERoleMemberType.class))
				.addColumn(
						new TablePagerColumn("memberId", $m("RoleMembersPage.3"), 150).setSort(false))
				.addColumn(
						new TablePagerColumn("primaryRole", $m("RoleMembersPage.4"), 65).setSort(false)
								.setPropertyClass(Boolean.class))
				.addColumn(new TablePagerColumn("deptId", $m("RoleMembersPage.5"), 100).setSort(false))
				.addColumn(TablePagerColumn.DESCRIPTION()).addColumn(TablePagerColumn.OPE(80));
		return tablePager;
	}

	protected Class<? extends AbstractTemplatePage> getAddMembersPageClass() {
		return AddMembersPage.class;
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("mId"), ";");
		orgContext.getRoleMemberService().delete(ids);
		return new JavascriptForward("$Actions['RoleMemberPage_tbl']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doPrimaryRole(final ComponentParameter cp) {
		final IRoleMemberService service = orgContext.getRoleMemberService();
		service.setPrimary(service.getBean(cp.getParameter("mId")));
		return new JavascriptForward("$Actions['RoleMemberPage_tbl']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doRoleSave(final ComponentParameter cp) {
		final IRoleService service = orgContext.getRoleService();
		final Role role = service.getBean(cp.getParameter("roleId"));
		if (role != null) {
			final String ruleValue = cp.getParameter("role_ruleValue");
			if (role.getRoleType() == ERoleType.handle) {
				role.setRuleHandler(ruleValue);
				service.update(new String[] { "rulehandler" }, role);
			} else {
				role.setRuleScript(ruleValue);
				service.update(new String[] { "rulescript" }, role);
			}
			return new JavascriptForward("alert('").append($m("RoleMembersPage.12")).append("');");
		} else {
			return null;
		}
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doMove(final ComponentParameter cp) {
		final IRoleMemberService service = orgContext.getRoleMemberService();
		final RoleMember item = service.getBean(cp.getParameter(TablePagerUtils.PARAM_MOVE_ROWID));
		final RoleMember item2 = service.getBean(cp.getParameter(TablePagerUtils.PARAM_MOVE_ROWID2));
		if (item != null && item2 != null) {
			service.exchange(item, item2,
					Convert.toBool(cp.getParameter(TablePagerUtils.PARAM_MOVE_UP)));
		}
		return new JavascriptForward("$Actions['RoleMemberPage_tbl']();");
	}

	private Role getRoleCache(final PageRequestResponse rRequest) {
		return rRequest.getRequestCache("@roleId", new CacheV<Role>() {
			@Override
			public Role get() {
				return orgContext.getRoleService().getBean(rRequest.getParameter("roleId"));
			}
		});
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String variable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='RoleMembersPage'>");
		sb.append("<div class='tb'>");
		sb.append("<div class='nav_arrow'>");
		final Role role = getRoleCache(pp);
		ERoleType rt = null;
		if (role != null) {
			rt = role.getRoleType();
			sb.append(role.getText());
			sb.append(SpanElement.shortText("(" + role.getName() + ")"));
		} else {
			sb.append("#(RoleMembersPage.0)");
		}

		sb.append("</div><div class='btn'>");
		final String aClass = "simple_btn simple_btn_all";
		if (rt == ERoleType.normal) {
			sb.append("<a class='").append(aClass)
					.append("' onclick=\"$Actions['addMemberWindow']('roleId=").append(role.getId())
					.append("');\">#(RoleMembersPage.1)</a>");
			sb.append(HtmlConst.NBSP).append("<a class='").append(aClass)
					.append("' onclick=\"this.up('.RoleMgrPage').deleteMember();\">#(Delete)</a>");
		} else if (rt == ERoleType.handle) {
			sb.append("<a class='").append(aClass).append("' onclick=\"")
					.append("$Actions['ajax_roleSave']($Form('#idRoleMemberVal .rule'));")
					.append("\">#(RoleMembersPage.8)</a>");
		} else if (rt == ERoleType.script) {
			sb.append("<a class='").append(aClass).append("' onclick=\"")
					.append("$Actions['ajax_roleSave']($Form('#idRoleMemberVal .rule'));")
					.append("\">#(RoleMembersPage.8)</a>");
			sb.append(HtmlConst.NBSP).append("<a class='").append(aClass).append("' onclick=\"")
					.append("\">#(RoleMembersPage.9)</a>");
		}
		sb.append("</div>");
		sb.append(BlockElement.CLEAR);
		sb.append("</div>");

		if (rt == ERoleType.normal) {
			sb.append("<div id='idMemberTable'></div>");
		} else {
			sb.append("<div  class='rule'>");
			if (role != null) {
				sb.append("<input type='hidden' name='roleId' value='");
				sb.append(role.getId()).append("' />");
			}
			if (rt == ERoleType.handle) {
				sb.append("<div class='t'>#(RoleMembersPage.10)").append(HtmlConst.NBSP)
						.append(IRoleHandler.class.getName()).append("</div>");
				sb.append("<div class='c'><textarea name='role_ruleValue' rows='1'>");
				final IRoleHandler rHandler = orgContext.getRoleService().getRoleHandler(role);
				if (rHandler != null) {
					sb.append(rHandler.getClass().getName());
				}
				sb.append("</textarea>");
			} else if (rt == ERoleType.script) {
				sb.append("<div class='t'>#(RoleMembersPage.11)</div>");
				sb.append("<div class='c'><textarea name='role_ruleValue' rows='14'>");
				sb.append(StringUtils.blank(role.getRuleScript())).append("</textarea>");
			}
			sb.append("</div></div>");
		}
		sb.append("</div>");
		return sb.toString();
	}

	public static class MemberTable extends AbstractDbTablePagerHandler {

		@Override
		public Map<String, Object> getFormParameters(final ComponentParameter cp) {
			final Role role = AbstractMVCPage.get(RoleMembersPage.class).getRoleCache(cp);
			return ((KVMap) super.getFormParameters(cp)).add("roleId", role.getId());
		}

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Role role = AbstractMVCPage.get(RoleMembersPage.class).getRoleCache(cp);
			return orgContext.getRoleService().members(role);
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
						.setOnclick("$Actions['ajax_editPrimaryRole']('mId=" + id + "');"));
				ID deptId = rm.getDeptId();
				if (deptId == null) {
					final User bean = _userService.getBean(rm.getMemberId());
					deptId = bean.getDepartmentId();
				}
				kv.put("deptId", AccountMgrPageUtils.toDepartmentText(deptId));
			}

			kv.put(TablePagerColumn.DESCRIPTION, rm.getDescription());

			final StringBuilder sb = new StringBuilder();
			sb.append(ButtonElement.deleteBtn().setOnclick(
					"$Actions['ajax_deleteMember']('mId=" + id + "');"));
			sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
			kv.put(TablePagerColumn.OPE, sb.toString());
			return kv;
		}

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			if (menuItem == null) {
				final MenuItems items = MenuItems.of(MenuItem.itemDelete().setOnclick_act(
						"ajax_deleteMember", "mId"));
				items.append(MenuItem.sep());
				items.append(MenuItem
						.of($m("Menu.move"))
						.addChild(
								MenuItem.of($m("Menu.up"), MenuItem.ICON_UP,
										"$pager_action(item).move(true, 'RoleMemberPage_Move');"))
						.addChild(
								MenuItem.of($m("Menu.up2"), MenuItem.ICON_UP2,
										"$pager_action(item).move2(true, 'RoleMemberPage_Move');"))
						.addChild(
								MenuItem.of($m("Menu.down"), MenuItem.ICON_DOWN,
										"$pager_action(item).move(false, 'RoleMemberPage_Move');"))
						.addChild(
								MenuItem.of($m("Menu.down2"), MenuItem.ICON_DOWN2,
										"$pager_action(item).move2(false, 'RoleMemberPage_Move');")));
				return items;
			}
			return null;
		}
	}
}
