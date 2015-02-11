package net.simpleframework.organization.web.page2;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
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
import net.simpleframework.organization.User;
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

		addComponentBean(pp, "UserMgrTPage_dept", TreeBean.class).setContainerId(
				"idUserMgrTPage_dept").setHandlerClass(DepartmentTree.class);

		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp, "UserMgrTPage_tbl")
				.setPagerBarLayout(EPagerBarLayout.bottom).setPageItems(30)
				.setContainerId("idUserMgrTPage_tbl").setHandlerClass(UserTbl.class);
		AccountMgrPageUtils.addAccountTblCols(tablePager);
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='tbar'>");
		sb.append(ElementList.of(LinkButton.addBtn(), SpanElement.SPACE, LinkButton.deleteBtn()));
		sb.append("</div>");
		sb.append("<div id='idUserMgrTPage_tbl'>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class DepartmentTree extends AbstractTreeHandler {
		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			final TreeBean treeBean = (TreeBean) cp.componentBean;
			final IDepartmentService dService = orgContext.getDepartmentService();
			final TreeNodes nodes = TreeNodes.of();
			if (parent == null) {
				final Department org = dService.getBean(cp.getLogin().getDept().getDomainId());
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
			final Department org = orgContext.getDepartmentService().getBean(
					cp.getLogin().getDept().getDomainId());
			return org != null ? orgContext.getUserService().queryUsers(org) : null;
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
			sb.append(new ButtonElement("角色")).append(SpanElement.SPACE)
					.append(ButtonElement.editBtn());
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			data.add(TablePagerColumn.OPE, sb.toString());
			return data;
		}
	}
}