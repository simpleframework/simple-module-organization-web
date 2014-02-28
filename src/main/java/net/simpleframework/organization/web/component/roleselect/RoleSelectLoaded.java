package net.simpleframework.organization.web.component.roleselect;

import java.util.Collection;

import net.simpleframework.common.StringUtils;
import net.simpleframework.mvc.DefaultPageHandler;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryTreeHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.web.component.chartselect.RoleChartSelectBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleSelectLoaded extends DefaultPageHandler implements IOrganizationContextAware {

	@Override
	public void onBeforeComponentRender(final PageParameter pp) {
		super.onBeforeComponentRender(pp);

		final ComponentParameter nCP = RoleSelectUtils.get(pp);
		final String hashId = nCP.hashId();
		final String selectName = nCP.getComponentName();

		// roleChart选择器
		final RoleChartSelectBean roleChartSelect = (RoleChartSelectBean) pp.addComponentBean(
				selectName + "_chart", RoleChartSelectBean.class).setClearAction("false");

		final StringBuilder sb = new StringBuilder();
		sb.append("var s = selects[0];");
		sb.append("$Actions['").append(selectName).append("_tree'].refresh('chartId=' + s.id);");
		sb.append("$Actions['").append(selectName).append("_chart'].trigger.update(s.text);");
		sb.append("return true;");
		roleChartSelect.setJsSelectCallback(sb.toString());

		final String roleChartHandle = (String) nCP.getBeanProperty("roleChartHandler");
		if (StringUtils.hasText(roleChartHandle)) {
			roleChartSelect.setHandlerClass(roleChartHandle);
		}

		// 角色树
		pp.addComponentBean(selectName + "_tree", TreeBean.class)
				.setContainerId("container_" + hashId).setHandlerClass(RoleSelectTree.class)
				.setSelector(".role_select form");
	}

	public static class RoleSelectTree extends DictionaryTreeHandler {
		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			final ComponentParameter nCP = RoleSelectUtils.get(cp);
			final IRoleSelectHandle hdl = (IRoleSelectHandle) nCP.getComponentHandler();
			final Collection<Role> coll = hdl.roles(nCP, RoleSelectUtils.getRoleChart(nCP),
					(parent == null ? null : (Role) parent.getDataObject()));
			TreeNodes nodes = null;
			if (coll != null) {
				nodes = TreeNodes.of();
				final String name = nCP.getComponentName();
				for (final Role r : coll) {
					final TreeNode tn = new TreeNode((TreeBean) cp.componentBean, parent, r);
					tn.setImage(RoleSelectUtils.icon_role(nCP, r));
					tn.setJsDblclickCallback("selected_" + name + "(branch, ev);");
					nodes.add(tn);
				}
			}
			return nodes;
		}
	}
}
