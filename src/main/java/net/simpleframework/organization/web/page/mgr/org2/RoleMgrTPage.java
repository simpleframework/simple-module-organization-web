package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ado.query.ListDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.TablePagerUtils;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleChart;
import net.simpleframework.organization.web.page.mgr.OmgrUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleMgrTPage extends AbstractOrgMgrTPage {
	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addTablePagerBean(pp);

		// AjaxRequestBean ajaxRequest = addAjaxRequest(pp,
		// "RoleMgrTPage_membersPage",
		// _RoleMembersPage.class);
		// addWindowBean(pp, "RoleMgrTPage_members",
		// ajaxRequest).setTitle($m("RoleMgrTPage.5"))
		// .setWidth(800).setHeight(480);

		// 添加角色
		final AjaxRequestBean ajaxRequest = (AjaxRequestBean) addAjaxRequest(pp,
				"RoleMgrTPage_rolePage", RoleEditPage.class).setSelector(
				"#idRoleMgrTPage_tbl .parameters");
		addWindowBean(pp, "RoleMgrTPage_roleWin", ajaxRequest).setTitle($m("RoleMgrTPage.5"))
				.setWidth(340).setHeight(360);

		// 删除角色
		addDeleteAjaxRequest(pp, "RoleMgrTPage_delete");
		// 移动
		addAjaxRequest(pp, "RoleMgrTPage_Move").setHandlerMethod("doMove");
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp, "RoleMgrTPage_tbl")
				.setContainerId("idRoleMgrTPage_tbl").setHandlerClass(RoleTbl.class);
		tablePager
				.addColumn(new TablePagerColumn("text", $m("RoleMgrTPage.0")).setSort(false))
				.addColumn(new TablePagerColumn("name", $m("RoleMgrTPage.1"), 150).setSort(false))
				.addColumn(
						new TablePagerColumn("roletype", $m("RoleMgrTPage.2"), 90).setFilterSort(false))
				.addColumn(
						new TablePagerColumn("members", $m("RoleMgrTPage.4"), 50).setTextAlign(
								ETextAlign.center).setFilterSort(false))
				.addColumn(TablePagerColumn.OPE(70));
		return tablePager;
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		_roleService.delete(cp.getParameter("id"));
		return new JavascriptForward("$Actions['RoleMgrTPage_tbl']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doMove(final ComponentParameter cp) {
		_roleService.exchange(TablePagerUtils.getExchangeBeans(cp, _roleService));
		return new JavascriptForward("$Actions['RoleMgrTPage_tbl']();");
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='RoleMgrTPage clearfix'><table width='100%'><tr>");
		sb.append(" <td valign='top' style='width: 17%;'><div class='lnav'>");
		sb.append("  <div class='lbl'>#(RoleMgrTPage.3)</div>");
		final RoleChart _chart = _getRoleChart(pp);
		if (_chart != null) {
			final IDataQuery<RoleChart> dq = _rolecService.queryOrgCharts(_deptService.getBean(_chart
					.getOrgId()));
			RoleChart chart;
			while ((chart = dq.next()) != null) {
				sb.append("<div class='litem");
				if (chart.equals(_chart)) {
					sb.append(" active");
				}
				sb.append("' onclick=\"$Actions.reloc('chartId=").append(chart.getId())
						.append("');\">").append(chart.getText());
				final int roles = chart.getRoles();
				if (roles > 0) {
					sb.append(new SpanElement("(" + roles + ")").setClassName("num"));
				}
				sb.append("</div>");
			}
		}
		sb.append(" </div></td>");
		sb.append(" <td valign='top'><div class='rtbl'>");
		if (_chart != null) {
			sb.append("  <div class='tbar'>");
			sb.append(ElementList.of(
					LinkButton.addBtn().setOnclick("$Actions['RoleMgrTPage_roleWin']();"),
					SpanElement.SPACE, LinkButton.deleteBtn()));
			sb.append("  </div>");
		}
		sb.append("  <div id='idRoleMgrTPage_tbl'></div>");
		sb.append(" </div></td>");
		sb.append("</tr></table></div>");
		return sb.toString();
	}

	private static RoleChart _getRoleChart(final PageParameter pp) {
		final Department org = getOrg2(pp);
		if (org != null) {
			RoleChart rchart = OmgrUtils.getRoleChart(pp);
			if (rchart == null || !rchart.getOrgId().equals(org.getId())) {
				rchart = _rolecService.queryOrgCharts(org).next();
			}
			return rchart;
		}
		return null;
	}

	public static class RoleTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final RoleChart rchart = _getRoleChart(cp);
			if (rchart != null) {
				cp.addFormParameter("orgId", rchart.getOrgId());
				cp.addFormParameter("chartId", rchart.getId());
				return new ListDataQuery<Role>(list(rchart, null));
			}
			return null;
		}

		private List<Role> list(final RoleChart chart, final Role parent) {
			final List<Role> l = new ArrayList<Role>();

			final IDataQuery<Role> dq = parent == null ? _roleService.queryRoot(chart) : _roleService
					.queryChildren(parent);
			Role role;
			while ((role = dq.next()) != null) {
				l.add(role);
				role.setAttr("_margin", parent != null ? Convert.toInt(parent.getAttr("_margin")) + 1
						: 1);
				l.addAll(list(chart, role));
			}
			return l;
		}

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			if (menuItem != null) {
				return null;
			}
			final MenuItems items = MenuItems.of();
			items.add(MenuItem.itemDelete().setOnclick_act("RoleMgrTPage_delete", "id"));
			items.add(MenuItem.sep());
			items.append(MenuItem
					.of($m("Menu.move"))
					.addChild(
							MenuItem.of($m("Menu.up"), MenuItem.ICON_UP,
									"$pager_action(item).move(true, 'RoleMgrTPage_Move');"))
					.addChild(
							MenuItem.of($m("Menu.up2"), MenuItem.ICON_UP2,
									"$pager_action(item).move2(true, 'RoleMgrTPage_Move');"))
					.addChild(
							MenuItem.of($m("Menu.down"), MenuItem.ICON_DOWN,
									"$pager_action(item).move(false, 'RoleMgrTPage_Move');"))
					.addChild(
							MenuItem.of($m("Menu.down2"), MenuItem.ICON_DOWN2,
									"$pager_action(item).move2(false, 'RoleMgrTPage_Move');")));
			return items;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final Role role = (Role) dataObject;
			final KVMap kv = new KVMap();
			final StringBuilder txt = new StringBuilder();
			for (int i = 0; i < Convert.toInt(role.getAttr("_margin")); i++) {
				txt.append(i == 0 ? "| -- " : " -- ");
			}
			txt.append(role.getText());
			kv.add("text", txt).add("name", _roleService.toUniqueName(role));

			kv.put(
					"members",
					LinkElement.style2(role.getMembers()).setOnclick(
							JS.loc(uFactory.getUrl(cp, RoleMgr_MembersTPage.class,
									"roleId=" + role.getId()))));
			kv.add("roletype", role.getRoleType()).add(TablePagerColumn.OPE, toOpeHTML(cp, role));
			return kv;
		}

		protected String toOpeHTML(final ComponentParameter cp, final Role role) {
			final StringBuilder sb = new StringBuilder();
			sb.append(ButtonElement.editBtn().setOnclick(
					"$Actions['RoleMgrTPage_roleWin']('roleId=" + role.getId() + "');"));
			sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
			return sb.toString();
		}
	}
}