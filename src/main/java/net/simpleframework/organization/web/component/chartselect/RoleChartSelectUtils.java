package net.simpleframework.organization.web.component.chartselect;

import java.util.Collection;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ComponentUtils;
import net.simpleframework.mvc.component.ext.deptselect.DeptSelectUtils;
import net.simpleframework.mvc.component.ui.tree.ITreeNodeAttributesCallback;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.organization.bean.Department;
import net.simpleframework.organization.bean.Department.EDepartmentType;
import net.simpleframework.organization.bean.RoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class RoleChartSelectUtils {

	public static String getRolechartIcon(final PageParameter pp, final String filename) {
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
				tn.setImage(getRolechartIcon(cp, "chart.png"));
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
			final EDepartmentType departmentType, final ITreeNodeAttributesCallback callback) {
		final TreeNodes nodes = TreeNodes.of();
		if (coll != null) {
			for (final Department dept2 : coll) {
				if (departmentType != null && dept2.getDepartmentType() != departmentType) {
					continue;
				}
				final TreeNode tn = new TreeNode(treeBean, parent, dept2);
				tn.setImage(DeptSelectUtils.getIconPath(cp,
						dept2.getDepartmentType() == EDepartmentType.organization));
				if (callback != null) {
					callback.setAttributes(tn);
				}
				nodes.add(tn);
			}
		}
		return nodes;
	}
}
