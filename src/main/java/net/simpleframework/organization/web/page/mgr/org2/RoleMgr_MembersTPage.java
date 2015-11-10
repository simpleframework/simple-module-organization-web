package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.tree.AbstractTreeHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.Department.EDepartmentType;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.Role.ERoleType;
import net.simpleframework.organization.RoleChart;
import net.simpleframework.organization.impl.OrganizationContext;
import net.simpleframework.organization.web.page.mgr.AddMembersPage;
import net.simpleframework.organization.web.page.mgr.OmgrUtils;
import net.simpleframework.organization.web.page.mgr.t1.RoleMembersPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleMgr_MembersTPage extends AbstractOrgMgrTPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 部门树
		addComponentBean(pp, "RoleMgr_MembersTPage_tree", TreeBean.class).setContainerId(
				"idRoleMgr_MembersTPage_dept").setHandlerClass(_DeptHandler.class);
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='RoleMgr_MembersTPage clearfix'>");
		sb.append("<table width='100%'><tr>");
		sb.append(" <td class='lnav'>");
		sb.append("  <div class='lbl'>");
		final RoleChart rchart = OmgrUtils.getRoleChart(pp);
		final Role r = OmgrUtils.getRole(pp);
		if (r != null) {
			sb.append(rchart).append(SpanElement.NAV()).append(r);
		}
		sb.append("  </div>");
		sb.append("  <div id='idRoleMgr_MembersTPage_dept'></div>");
		sb.append(" </td>");
		sb.append(" <td class='rtbl'>");
		sb.append("  <div class='tbar clearfix'>");
		sb.append("   <div class='left'>");
		String backUrl = uFactory.getUrl(pp, RoleMgrTPage.class);
		if (rchart != null) {
			backUrl += "?chartId=" + rchart.getId();
		}
		sb.append(LinkButton.backBtn().corner().setOnclick(JS.loc(backUrl)));
		sb.append(SpanElement.SPACE);
		sb.append(LinkButton.corner($m("RoleMgr_MembersTPage.0")).setClassName("showall")
				.setOnclick("$Actions['RoleMembersPage_tbl']('deptId=');"));
		sb.append("   </div>");
		sb.append("   <div class='right'>").append(RoleMembersPage.getActionElements(pp));
		sb.append("</div>");
		sb.append("   ");
		sb.append("  </div>");
		sb.append("  <div id='idRoleMgr_MembersTPage_tbl'>")
				.append(pp.includeUrl(_RoleMembersPage.class)).append("</div>");
		sb.append(" </td>");
		sb.append("</tr></table>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class _DeptHandler extends AbstractTreeHandler {

		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			final Role r = OmgrUtils.getRole(cp);
			final Map<Department, Integer> stat = cp.getRequestCache("_deptstat",
					new CacheV<Map<Department, Integer>>() {
						@Override
						public Map<Department, Integer> get() {
							return _rolemService.getMemberNums(r);
						}
					});
			final TreeNodes nodes = TreeNodes.of();
			final TreeBean treeBean = (TreeBean) cp.componentBean;
			if (parent == null) {
				final Department dept = getOrg2(cp);
				if (dept != null) {
					nodes.add(createTreeNode(treeBean, parent, dept, stat));
				}
			} else {
				final Object dataObject = parent.getDataObject();
				if (dataObject instanceof Department) {
					final IDataQuery<Department> dq = _deptService.queryDepartments(
							(Department) dataObject, EDepartmentType.department);
					Department dept;
					while ((dept = dq.next()) != null) {
						nodes.add(createTreeNode(treeBean, parent, dept, stat));
					}
				}
			}
			return nodes;
		}

		private TreeNode createTreeNode(final TreeBean treeBean, final TreeNode parent,
				final Object dataObject, final Map<Department, Integer> stat) {
			final TreeNode treeNode = new TreeNode(treeBean, parent, dataObject);
			String js = "$Actions['RoleMembersPage_tbl']('deptId=";
			if (dataObject instanceof Department) {
				final Department dept = (Department) dataObject;
				js += dept.getId();
				final int nums = Convert.toInt(stat.get(dept));
				if (nums > 0) {
					treeNode.setPostfixText("(" + nums + ")");
				}
			}
			js += "');";
			treeNode.setJsClickCallback(js);
			return treeNode;
		}
	}

	public static class _RoleMembersPage extends RoleMembersPage {

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String variable) throws IOException {
			final StringBuilder sb = new StringBuilder();
			final Role role = OmgrUtils.getRole(pp);
			final ERoleType rt = role != null ? role.getRoleType() : null;
			if (rt == ERoleType.normal) {
				sb.append("<div id='idRoleMembersPage_tbl'></div>");
			}
			return sb.toString();
		}

		@Override
		protected TablePagerBean addTablePagerBean(final PageParameter pp) {
			final StringBuilder sb = new StringBuilder();
			sb.append("var tbl = $('#idRoleMembersPage_tbl');");
			sb.append("var showall = tbl.up('.rtbl').down('.tbar .showall');");
			sb.append("var dept = tbl.down('.parameters #deptId');");
			sb.append("if ($F(dept).length > 0) showall.show();");
			sb.append("else showall.hide();");
			final TablePagerBean tablePager = (TablePagerBean) super.addTablePagerBean(pp)
					.setJsLoadedCallback(sb.toString());
			return tablePager;
		}

		@Override
		public String getPageRole(final PageParameter pp) {
			return OrganizationContext.ROLE_ORGANIZATION_MANAGER;
		}

		@Override
		protected Class<? extends AbstractTemplatePage> getAddMembersPageClass() {
			return _AddMembersPage.class;
		}
	}

	public static class _AddMembersPage extends AddMembersPage {
		@Override
		public String getPageRole(final PageParameter pp) {
			return OrganizationContext.ROLE_ORGANIZATION_MANAGER;
		}

		@Override
		protected JavascriptForward toJavascriptForward(final ComponentParameter cp, final Role role) {
			return new JavascriptForward("$Actions['RoleMembersPage_tbl']('deptId=');");
		}
	}
}
