package net.simpleframework.organization.web.page.mgr.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.organization.ERoleMemberType;
import net.simpleframework.organization.ERoleType;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRole;
import net.simpleframework.organization.IRoleHandler;
import net.simpleframework.organization.IRoleMember;
import net.simpleframework.organization.IRoleMemberService;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.web.page.mgr.AddMemberPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class RoleMemberPage extends AbstractTemplatePage implements IOrganizationContextAware {

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		addAjaxRequest(pp, "ajax_addMemberPage", AddMemberPage.class);
		addComponentBean(pp, "addMemberWindow", WindowBean.class).setContentRef("ajax_addMemberPage")
				.setTitle($m("RoleMemberPage.1")).setHeight(300).setWidth(320);

		// 删除成员
		addDeleteAjaxRequest(pp, "ajax_deleteMember");

		addAjaxRequest(pp, "ajax_editPrimaryRole").setHandleMethod("doPrimaryRole");

		// 成员列表
		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp, "memberTable",
				TablePagerBean.class).setShowLineNo(true).setPagerBarLayout(EPagerBarLayout.none)
				.setPageItems(Integer.MAX_VALUE).setContainerId("idMemberTable")
				.setHandleClass(MemberTable.class);
		tablePager
				.addColumn(
						new TablePagerColumn("memberType", $m("RoleMemberPage.2"), 110)
								.setPropertyClass(ERoleMemberType.class))
				.addColumn(
						new TablePagerColumn("memberId", $m("RoleMemberPage.3"), 110).setFilter(false)
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("primaryRole", $m("RoleMemberPage.4"), 100)
								.setPropertyClass(Boolean.class).setTextAlign(ETextAlign.left))
				.addColumn(TablePagerColumn.DESCRIPTION())
				.addColumn(TablePagerColumn.OPE().setWidth(80));

		// 保存规则角色
		addAjaxRequest(pp, "ajax_roleSave").setHandleMethod("doRoleSave");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("mId"), ";");
		if (ids != null) {
			context.getRoleMemberService().delete(ids);
		}
		return new JavascriptForward("$Actions['memberTable']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doPrimaryRole(final ComponentParameter cp) {
		final IRoleMemberService service = context.getRoleMemberService();
		service.setPrimary(service.getBean(cp.getParameter("mId")));
		return new JavascriptForward("$Actions['memberTable']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doRoleSave(final ComponentParameter cp) {
		final IRoleService service = context.getRoleService();
		final IRole role = service.getBean(cp.getParameter("roleId"));
		if (role != null) {
			final String ruleValue = cp.getParameter("role_ruleValue");
			if (role.getRoleType() == ERoleType.handle) {
				role.setRuleHandler(ruleValue);
				service.update(new String[] { "rulehandler" }, role);
			} else {
				role.setRuleScript(ruleValue);
				service.update(new String[] { "rulescript" }, role);
			}
			return new JavascriptForward("alert('").append($m("RoleMemberPage.12")).append("');");
		} else {
			return null;
		}
	}

	private IRole roleCache(final PageRequestResponse rRequest) {
		IRole role = (IRole) rRequest.getRequestAttr("@roleId");
		if (role != null) {
			return role;
		}
		final IRoleService service = context.getRoleService();
		role = service.getBean(rRequest.getParameter("roleId"));
		if (role != null) {
			rRequest.setRequestAttr("@roleId", role);
		}
		return role;
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String variable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='tb'>");
		sb.append("<div class='nav_arrow'>");
		final IRole role = roleCache(pp);
		ERoleType rt = null;
		if (role != null) {
			rt = role.getRoleType();
			sb.append(role.getText());
			sb.append(SpanElement.shortText("(" + role.getName() + ")"));
		} else {
			sb.append("#(RoleMemberPage.0)");
		}

		sb.append("</div><div class='btn'>");
		final String aClass = "simple_btn simple_btn_all";
		if (rt == ERoleType.normal) {
			sb.append("<a class='").append(aClass)
					.append("' onclick=\"$Actions['addMemberWindow']('roleId=").append(role.getId())
					.append("');\">#(RoleMemberPage.1)</a>");
			sb.append(NBSP).append("<a class='").append(aClass)
					.append("' onclick=\"this.up('.RoleMgrPage').deleteMember();\">#(Delete)</a>");
		} else if (rt == ERoleType.handle) {
			sb.append("<a class='").append(aClass).append("' onclick=\"")
					.append("$Actions['ajax_roleSave']($Form('#idRoleMemberVal .rule'));")
					.append("\">#(RoleMemberPage.8)</a>");
		} else if (rt == ERoleType.script) {
			sb.append("<a class='").append(aClass).append("' onclick=\"")
					.append("$Actions['ajax_roleSave']($Form('#idRoleMemberVal .rule'));")
					.append("\">#(RoleMemberPage.8)</a>");
			sb.append(NBSP).append("<a class='").append(aClass).append("' onclick=\"")
					.append("\">#(RoleMemberPage.9)</a>");
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
				sb.append("<div class='t'>#(RoleMemberPage.10)").append(NBSP)
						.append(IRoleHandler.class.getName()).append("</div>");
				sb.append("<div class='c'><textarea name='role_ruleValue' rows='1'>");
				final IRoleHandler rHandler = context.getRoleService().getRoleHandler(role);
				if (rHandler != null) {
					sb.append(rHandler.getClass().getName());
				}
				sb.append("</textarea>");
			} else if (rt == ERoleType.script) {
				sb.append("<div class='t'>#(RoleMemberPage.11)</div>");
				sb.append("<div class='c'><textarea name='role_ruleValue' rows='14'>");
				sb.append(StringUtils.blank(role.getRuleScript())).append("</textarea>");
			}
			sb.append("</div></div>");
		}
		return sb.toString();
	}

	public static class MemberTable extends AbstractDbTablePagerHandler {

		@Override
		public Map<String, Object> getFormParameters(final ComponentParameter cp) {
			final IRole role = AbstractMVCPage.get(RoleMemberPage.class).roleCache(cp);
			return ((KVMap) super.getFormParameters(cp)).add("roleId", role.getId());
		}

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final IRole role = AbstractMVCPage.get(RoleMemberPage.class).roleCache(cp);
			return context.getRoleService().members(role);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final IRoleMember rm = (IRoleMember) dataObject;
			final ID id = rm.getId();
			final KVMap kv = new KVMap();
			final ERoleMemberType mType = rm.getMemberType();
			kv.put("memberType", mType);
			final IDbBeanService<?> mgr = (mType == ERoleMemberType.user ? context.getUserService()
					: context.getRoleService());
			kv.put("memberId", mgr.getBean(rm.getMemberId()));
			if (mType == ERoleMemberType.user) {
				final StringBuilder sb = new StringBuilder();
				final boolean pr = rm.isPrimaryRole();
				sb.append(pr ? $m("RoleMemberPage.5") : $m("RoleMemberPage.6"));
				if (!pr) {
					sb.append(SpanElement.SPACE).append(
							new ButtonElement($m("RoleMemberPage.7"))
									.setOnclick("$Actions['ajax_editPrimaryRole']('mId=" + id + "');"));
				}
				kv.put("primaryRole", sb.toString());
			}
			kv.put(TablePagerColumn.DESCRIPTION, rm.getDescription());
			kv.put(
					TablePagerColumn.OPE,
					ButtonElement.deleteBtn().setOnclick(
							"$Actions['ajax_deleteMember']('mId=" + id + "');"));
			return kv;
		}
	}
}
