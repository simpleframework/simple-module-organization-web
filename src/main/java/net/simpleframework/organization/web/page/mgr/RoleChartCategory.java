package net.simpleframework.organization.web.page.mgr;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.component.AbstractComponentBean;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.category.ctx.CategoryBeanAwareHandler;
import net.simpleframework.mvc.component.ui.menu.AbstractMenuHandler;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.tree.ITreeNodeAttributesCallback;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleChartService;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.RoleChart;
import net.simpleframework.organization.web.component.chartselect.RoleChartSelectUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleChartCategory extends CategoryBeanAwareHandler<RoleChart> implements
		IOrganizationContextAware {

	@Override
	protected IRoleChartService getBeanService() {
		return orgContext.getRoleChartService();
	}

	@Override
	public TreeNodes getCategoryTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode parent) {
		final TreeNodes nodes = TreeNodes.of();
		if (parent == null) {
			TreeNode tn = createRoot(treeBean, $m("RoleChartCategory.0"), $m("RoleChartCategory.1"));
			tn.setImage(RoleChartSelectUtils.icon_chart(cp, "chart_g.png"));
			tn.setContextMenu("none");
			nodes.add(tn);
			tn = new TreeNode(treeBean, parent, $m("RoleChartCategory.2"));
			tn.setOpened(true);
			tn.setImage(RoleChartSelectUtils.icon_chart(cp, "chart_d.png"));
			tn.setContextMenu("none");
			nodes.add(tn);
		} else {
			final String image = parent.getImage();
			if (image != null) {
				if (image.endsWith("chart_g.png")) {
					nodes.addAll(rolecharts(cp, treeBean, parent, null));
				} else if (image.endsWith("chart_d.png")) {
					nodes.addAll(departments(cp, treeBean, parent, null));
				} else {
					final Object dept = parent != null ? parent.getDataObject() : null;
					if (dept instanceof Department) {
						nodes.addAll(rolecharts(cp, treeBean, parent, (Department) dept));
						// 动态装载
						final TreeNodes _nodes = departments(cp, treeBean, parent, (Department) dept);
						for (final TreeNode tn : _nodes) {
							tn.setDynamicLoading(true);
						}
						nodes.addAll(_nodes);
					}
				}
			}
		}
		return nodes.size() > 0 ? nodes : null;
	}

	private TreeNodes rolecharts(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode parent, final Department dept) {
		final String contextMenu = treeBean.getContextMenu();
		final IRoleService service = orgContext.getRoleService();
		final IDataQuery<RoleChart> dq = dept == null ? getBeanService().queryGlobalCharts()
				: getBeanService().queryOrgCharts(dept);
		return RoleChartSelectUtils.rolecharts(cp, treeBean, parent, DataQueryUtils.toList(dq),
				new ITreeNodeAttributesCallback() {
					@Override
					public void setAttributes(final TreeNode tn) {
						tn.setContextMenu(contextMenu);
						final RoleChart chart = (RoleChart) tn.getDataObject();
						final int c = service.queryRoles(chart).getCount();
						if (c > 0) {
							tn.setPostfixText("(" + c + ")");
						}
						tn.setJsClickCallback("$Actions['roleCategory']('chartId=" + chart.getId()
								+ "');");
					}
				});
	}

	private TreeNodes departments(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode parent, final Department dept) {
		return RoleChartSelectUtils.departments(cp, treeBean, parent,
				DataQueryUtils.toList(orgContext.getDepartmentService().queryChildren(dept)),
				EDepartmentType.organization, new ITreeNodeAttributesCallback() {
					@Override
					public void setAttributes(final TreeNode tn) {
						tn.setContextMenu("roleChartCategory_DeptMenu");
					}
				});
	}

	@Override
	protected String[] getContextMenuKeys() {
		return new String[] { "Edit", "Delete", "-", "Refresh", "-", "Move" };
	}

	@Override
	protected void onSave_setProperties(final ComponentParameter cp, final RoleChart roleChart,
			final boolean insert) {
		if (insert) {
			final Department dept = orgContext.getDepartmentService().getBean(
					cp.getParameter(PARAM_CATEGORY_PARENTID));
			if (dept != null) {
				roleChart.setOrgId(dept.getId());
			}
		}
	}

	@Override
	public KVMap categoryEdit_attri(final ComponentParameter cp) {
		return ((KVMap) super.categoryEdit_attri(cp)).add(window_title, $m("RoleMgrPage.1")).add(
				window_height, 300);
	}

	@Override
	protected AbstractComponentBean categoryEdit_createPropEditor(final ComponentParameter cp) {
		final PropEditorBean editor = (PropEditorBean) super.categoryEdit_createPropEditor(cp);
		editor.getFormFields().remove(2);
		return editor;
	}

	public static class DeptContextMenu extends AbstractMenuHandler {
		@Override
		public MenuItems getMenuItems(final ComponentParameter cp, final MenuItem menuItem) {
			if (menuItem == null) {
				return MenuItems.of(new MenuItem().setTitle($m("RoleChartCategory.1")).setOnclick(
						"$category_action(item).add();"));
			}
			return null;
		}
	}
}
