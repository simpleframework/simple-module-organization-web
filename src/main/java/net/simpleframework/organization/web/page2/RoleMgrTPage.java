package net.simpleframework.organization.web.page2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ado.query.ListDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IRoleChartService;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleChart;
import net.simpleframework.organization.web.page.mgr.t1.RoleMembersPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleMgrTPage extends AbstractMgrTPage {
	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp, "RoleMgrTPage_tbl")
				.setPagerBarLayout(EPagerBarLayout.bottom).setPageItems(30)
				.setContainerId("idRoleMgrTPage_tbl").setHandlerClass(RoleTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("text", "显示名", 210).setTextAlign(ETextAlign.left).setSort(
								false))
				.addColumn(
						new TablePagerColumn("name", "角色名", 120).setTextAlign(ETextAlign.left).setSort(
								false))
				.addColumn(
						new TablePagerColumn("roletype", "角色类型", 90).setTextAlign(ETextAlign.left)
								.setSort(false)).addColumn(TablePagerColumn.DESCRIPTION())
				.addColumn(TablePagerColumn.OPE().setWidth(125));

		// 成员窗口
		addAjaxRequest(pp, "RoleMgrTPage_membersPage", _RoleMembersPage.class);
		addWindowBean(pp, "RoleMgrTPage_members").setContentRef("RoleMgrTPage_membersPage")
				.setTitle("角色成员").setWidth(800).setHeight(480);
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='RoleMgrTPage clearfix'>");
		sb.append(" <div class='lnav'>");
		sb.append("  <div class='lbl'>角色视图列表</div>");
		final Department org = getOrg(pp);
		if (org != null) {
			final IRoleChartService cService = orgContext.getRoleChartService();
			final RoleChart _chart = cService.getBean(pp.getParameter("chartId"));
			final IDataQuery<RoleChart> dq = cService.query(org);
			RoleChart chart;
			int i = 0;
			while ((chart = dq.next()) != null) {
				sb.append("<div class='litem");
				if ((i++ == 0 && (_chart == null || !_chart.getDepartmentId().equals(org.getId())))
						|| chart.equals(_chart)) {
					sb.append(" active");
				}
				sb.append("' onclick=\"location.href = location.href.addParameter('chartId=")
						.append(chart.getId()).append("');\">").append(chart.getText()).append("</div>");
			}
		}
		sb.append(" </div>");
		sb.append(" <div class='rtbl'>");
		sb.append("  <div class='tbar'>");
		sb.append(ElementList.of(LinkButton.addBtn(), SpanElement.SPACE, LinkButton.deleteBtn()));
		sb.append("  </div>");
		sb.append("  <div id='idRoleMgrTPage_tbl'></div>");
		sb.append(" </div>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class RoleTbl extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Department org = getOrg(cp);
			if (org != null) {
				final IRoleChartService cService = orgContext.getRoleChartService();
				RoleChart rchart = cService.getBean(cp.getParameter("chartId"));
				if (rchart == null) {
					rchart = cService.query(org).next();
				}
				if (rchart != null && rchart.getDepartmentId().equals(org.getId())) {
					cp.addFormParameter("orgId", org.getId());
					cp.addFormParameter("chartId", rchart.getId());
					return new ListDataQuery<Role>(list(rchart, null));
				}
			}
			return null;
		}

		final IRoleService rService = orgContext.getRoleService();

		private List<Role> list(final RoleChart chart, final Role parent) {
			final List<Role> l = new ArrayList<Role>();

			final IDataQuery<Role> dq = parent == null ? rService.queryRoot(chart) : rService
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
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final Role role = (Role) dataObject;
			final KVMap data = new KVMap();
			final StringBuilder txt = new StringBuilder();
			for (int i = 0; i < Convert.toInt(role.getAttr("_margin")); i++) {
				txt.append(i == 0 ? "| -- " : " -- ");
			}
			txt.append(role.getText());
			data.add("text", txt.toString());
			data.add("name", rService.toUniqueName(role));
			data.add("roletype", role.getRoleType());

			final StringBuilder sb = new StringBuilder();
			sb.append(
					new ButtonElement("成员").setOnclick("$Actions['RoleMgrTPage_members']('roleId="
							+ role.getId() + "');")).append(SpanElement.SPACE)
					.append(ButtonElement.editBtn());
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			data.add(TablePagerColumn.OPE, sb.toString());
			return data;
		}
	}

	public static class _RoleMembersPage extends RoleMembersPage {
		@Override
		public String getRole(final PageParameter pp) {
			return IOrganizationContext.ROLE_ORGANIZATION_MANAGER;
		}
	}
}