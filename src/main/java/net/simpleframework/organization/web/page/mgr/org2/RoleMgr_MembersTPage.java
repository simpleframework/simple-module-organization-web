package net.simpleframework.organization.web.page.mgr.org2;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.tree.AbstractTreeHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.web.page.mgr.OmgrUtils;

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
		sb.append(" <div class='lnav'>");
		sb.append("  <div class='lbl'>");
		final Role r = OmgrUtils.getRole(pp);
		if (r != null) {
			sb.append(OmgrUtils.getRoleChart(pp)).append(SpanElement.NAV).append(r);
		}
		sb.append("  </div>");
		sb.append("  <div id='idRoleMgr_MembersTPage_dept'></div>");
		sb.append(" </div>");
		sb.append(" <div class='rtbl'>");
		sb.append("  <div class='tbar'>");
		sb.append(LinkButton.backBtn().corner());
		sb.append("  </div>");
		sb.append("  <div id='idRoleMgr_MembersTPage_tbl'></div>");
		sb.append(" </div>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class _DeptHandler extends AbstractTreeHandler {

		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			final TreeNodes nodes = TreeNodes.of();
			return nodes;
		}
	}
}
