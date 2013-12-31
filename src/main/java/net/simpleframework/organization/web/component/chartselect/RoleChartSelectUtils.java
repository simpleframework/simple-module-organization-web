package net.simpleframework.organization.web.component.chartselect;

import java.util.Collection;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ComponentUtils;
import net.simpleframework.mvc.component.ui.tree.ITreeNodeAttributesCallback;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.ERoleChartMark;
import net.simpleframework.organization.RoleChart;
import net.simpleframework.organization.web.component.deptselect.DeptSelectUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class RoleChartSelectUtils {

	public static String icon_chart(final PageParameter pp, final RoleChart chart) {
		return icon_chart(pp, chart.getChartMark() == ERoleChartMark.builtIn ? "chart_lock.png"
				: "chart.png");
	}

	public static String icon_chart(final PageParameter pp, final String filename) {
		return ComponentUtils.getCssResourceHomePath(pp, RoleChartSelectBean.class) + "/images/"
				+ filename;
	}

	public static TreeNodes rolecharts(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode parent, final Collection<RoleChart> coll,
			final ITreeNodeAttributesCallback callback) {
		final TreeNodes nodes = TreeNodes.of();
		if (coll != null) {
			for (final RoleChart chart : coll) {
				final TreeNode tn = new TreeNode(treeBean, parent, chart);
				tn.setImage(icon_chart(cp, chart));
				if (callback != null) {
					callback.setAttributes(tn);
				}
				nodes.add(tn);
			}
		}
		return nodes;
	}

	public static TreeNodes departments(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode parent, final Collection<Department> coll,
			final ITreeNodeAttributesCallback callback) {
		final TreeNodes nodes = TreeNodes.of();
		if (coll != null) {
			for (final Department dept2 : coll) {
				final TreeNode tn = new TreeNode(treeBean, parent, dept2);
				tn.setImage(DeptSelectUtils.icon_dept(cp, dept2));
				if (callback != null) {
					callback.setAttributes(tn);
				}
				nodes.add(tn);
			}
		}
		return nodes;
	}
}
