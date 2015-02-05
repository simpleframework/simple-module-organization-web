package net.simpleframework.organization.web.page2;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.tree.AbstractTreeHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IDepartmentService;

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
				.setContainerId("idUserMgrTPage_tbl").setHandlerClass(UserTbl.class);
		tablePager
		// .addColumn(
		// new TablePagerColumn("text", $m("DepartmentMgrTPage.0")).setTextAlign(
		// ETextAlign.left).setSort(false))
		// .addColumn(
		// new TablePagerColumn("name", $m("DepartmentMgrTPage.1"),
		// 150).setTextAlign(
		// ETextAlign.left).setSort(false))
				.addColumn(TablePagerColumn.OPE().setWidth(80));
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
	}
}