package net.simpleframework.organization.web.component.chartselect;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentBean;
import net.simpleframework.mvc.component.ComponentName;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ComponentResourceProvider;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryRegistry;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryTreeHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.RoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@ComponentName(RoleChartSelectRegistry.ROLECHARTSELECT)
@ComponentBean(RoleChartSelectBean.class)
@ComponentResourceProvider(RoleChartSelectResourceProvider.class)
public class RoleChartSelectRegistry extends DictionaryRegistry {
	public static final String ROLECHARTSELECT = "roleChartSelect";

	@Override
	public RoleChartSelectBean createComponentBean(final PageParameter pp, final Object attriData) {
		final RoleChartSelectBean roleChart = (RoleChartSelectBean) super.createComponentBean(pp,
				attriData);

		final ComponentParameter nCP = ComponentParameter.get(pp, roleChart);

		final String chartSelectName = nCP.getComponentName();

		final TreeBean treeBean = (TreeBean) pp.addComponentBean(chartSelectName + "_tree",
				TreeBean.class).setHandlerClass(RoleChartTree.class);

		roleChart.addTreeRef(nCP, treeBean.getName());
		treeBean.setAttr("__roleChart", roleChart);
		return roleChart;
	}

	public static class RoleChartTree extends DictionaryTreeHandler implements
			IOrganizationContextAware {
		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			final TreeNodes nodes = TreeNodes.of();
			final TreeBean treeBean = (TreeBean) cp.componentBean;
			final ComponentParameter nCP = ComponentParameter.get(cp,
					(RoleChartSelectBean) treeBean.getAttr("__roleChart"));
			final IRoleChartSelectHandle hdl = (IRoleChartSelectHandle) nCP.getComponentHandler();
			final Department org = orgContext.getDepartmentService().getBean(cp.getParameter("orgId"));
			if (org != null) {
				if (parent == null) {
					nodes.addAll(RoleChartSelectUtils.rolecharts(cp, treeBean, parent,
							hdl.getRoleCharts(cp, treeBean, org), null));
				}
			} else {
				if (parent == null) {
					final boolean showGlobalChart = (Boolean) nCP.getBeanProperty("showGlobalChart");
					if (showGlobalChart) {
						final TreeNode n = new TreeNode(treeBean, null, $m("RoleChartSelectRegistry.0"));
						n.setImage(RoleChartSelectUtils.icon_chart(nCP, "chart_g.png"));
						n.setOpened(true);
						nodes.add(n);
					}
					final TreeNode n = new TreeNode(treeBean, null, $m("RoleChartSelectRegistry.1"));
					n.setImage(RoleChartSelectUtils.icon_chart(nCP, "chart_d.png"));
					n.setOpened(true);
					nodes.add(n);
				} else {
					final String image = parent.getImage();
					if (image != null) {
						if (image.endsWith("chart_g.png")) {
							nodes.addAll(RoleChartSelectUtils.rolecharts(cp, treeBean, parent,
									hdl.getRoleCharts(cp, treeBean, null), null));
						} else if (image.endsWith("chart_d.png")) {
							nodes.addAll(RoleChartSelectUtils.departments(nCP, treeBean, parent,
									hdl.getDepartments(nCP, treeBean, null), null, null));
						} else {
							final Object o = parent.getDataObject();
							if (o instanceof Department) {
								nodes.addAll(RoleChartSelectUtils.rolecharts(cp, treeBean, parent,
										hdl.getRoleCharts(cp, treeBean, (Department) o), null));
								nodes.addAll(RoleChartSelectUtils.departments(nCP, treeBean, parent,
										hdl.getDepartments(nCP, treeBean, (Department) o), null, null));
							}
						}
					}
				}
			}
			return nodes;
		}

		@Override
		public Map<String, Object> getTreenodeAttributes(final ComponentParameter cp,
				final TreeNode treeNode, final TreeNodes children) {
			final KVMap kv = (KVMap) super.getTreenodeAttributes(cp, treeNode, children);
			if (treeNode == null || !(treeNode.getDataObject() instanceof RoleChart)) {
				kv.put(TN_ATTRI_SELECT_DISABLE, Boolean.TRUE);
			}
			return kv;
		}
	}
}
