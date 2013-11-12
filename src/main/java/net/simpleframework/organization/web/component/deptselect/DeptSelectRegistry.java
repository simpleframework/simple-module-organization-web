package net.simpleframework.organization.web.component.deptselect;

import java.util.Collection;

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
import net.simpleframework.organization.IDepartment;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
@ComponentName(DeptSelectRegistry.DEPTSELECT)
@ComponentBean(DeptSelectBean.class)
@ComponentResourceProvider(DeptSelectResourceProvider.class)
public class DeptSelectRegistry extends DictionaryRegistry {
	public static final String DEPTSELECT = "deptSelect";

	@Override
	public DeptSelectBean createComponentBean(final PageParameter pp, final Object attriData) {
		final DeptSelectBean deptSelect = (DeptSelectBean) super.createComponentBean(pp, attriData);

		final ComponentParameter nCP = ComponentParameter.get(pp, deptSelect);

		final String deptSelectName = nCP.getComponentName();

		final TreeBean treeBean = (TreeBean) pp.addComponentBean(deptSelectName + "_tree",
				TreeBean.class).setHandleClass(DeptTree.class);

		deptSelect.addTreeRef(nCP, treeBean.getName());
		treeBean.setAttr("__deptSelect", deptSelect);

		return deptSelect;
	}

	public static class DeptTree extends DictionaryTreeHandler {

		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode treeNode) {
			final TreeBean treeBean = (TreeBean) cp.componentBean;
			final ComponentParameter nCP = ComponentParameter.get(cp,
					(DeptSelectBean) treeBean.getAttr("__deptSelect"));
			final IDeptSelectHandle hdl = (IDeptSelectHandle) nCP.getComponentHandler();
			IDepartment parent = null;
			if (treeNode != null) {
				parent = (IDepartment) treeNode.getDataObject();
			}
			final Collection<? extends IDepartment> coll = hdl.getDepartments(nCP, treeBean, parent);
			if (coll != null) {
				final TreeNodes nodes = TreeNodes.of();
				for (final IDepartment d : coll) {
					final TreeNode n = new TreeNode(treeBean, treeNode, d);
					n.setImage(DeptSelectUtils.icon_dept(nCP, d));
					n.setDynamicLoading(treeNode != null);
					nodes.add(n);
				}
				return nodes;
			}
			return null;
		}
	}
}
