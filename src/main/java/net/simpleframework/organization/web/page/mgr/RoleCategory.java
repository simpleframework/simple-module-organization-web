package net.simpleframework.organization.web.page.mgr;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.AbstractComponentBean;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.category.ctx.CategoryBeanAwareHandler;
import net.simpleframework.mvc.component.ui.propeditor.EInputCompType;
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.organization.ERoleType;
import net.simpleframework.organization.IDepartment;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRole;
import net.simpleframework.organization.IRoleChart;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.web.component.chartselect.RoleChartSelectUtils;
import net.simpleframework.organization.web.component.roleselect.RoleSelectUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class RoleCategory extends CategoryBeanAwareHandler<IRole> implements
		IOrganizationContextAware {

	@Override
	protected IRoleService getBeanService() {
		return context.getRoleService();
	}

	private IRoleChart getRoleChart(final PageRequestResponse rRequest) {
		IRoleChart roleChart = (IRoleChart) rRequest.getRequestAttr("@chartId");
		if (roleChart != null) {
			return roleChart;
		}
		roleChart = context.getRoleChartService().getBean(rRequest.getParameter("chartId"));
		if (roleChart == null) {
			roleChart = context.getSystemChart();
		}
		rRequest.setRequestAttr("@chartId", roleChart);
		return roleChart;
	}

	@Override
	protected IDataQuery<?> categoryBeans(final ComponentParameter cp, final Object categoryId) {
		final IRoleService service = getBeanService();
		final IRole parent = service.getBean(categoryId);
		if (parent == null) {
			return service.queryRoot(getRoleChart(cp));
		} else {
			return service.queryChildren(parent);
		}
	}

	@Override
	public Map<String, Object> getFormParameters(final ComponentParameter cp) {
		return ((KVMap) super.getFormParameters(cp)).add("chartId", getRoleChart(cp).getId());
	}

	@Override
	public TreeNodes getCategoryTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode parent) {
		if (parent == null) {
			final TreeNodes nodes = TreeNodes.of();
			final TreeNode tn = createRoot(treeBean, $m("RoleCategory.0"), $m("RoleCategory.1"));
			tn.setContextMenu("none");
			final IRoleChart chart = getRoleChart(cp);
			tn.setImage(RoleChartSelectUtils.icon_chart(cp, chart));
			tn.setAcceptdrop(true);
			nodes.add(tn);
			return nodes;
		} else {
			final Object o = parent.getDataObject();
			if (o instanceof IRole) {
				final IRole role = (IRole) o;
				parent.setImage(RoleSelectUtils.icon_role(cp, role));
				if (role.getRoleType() == ERoleType.normal) {
					final int count = context.getRoleMemberService().queryMembers(role).getCount();
					if (count > 0) {
						parent.setPostfixText("(" + count + ")");
					}
				}
				parent.setJsClickCallback("$Actions['ajaxRoleMemberVal']('roleId="
						+ ((IRole) o).getId() + "');");
			}
			return super.getCategoryTreenodes(cp, treeBean, parent);
		}
	}

	@Override
	public TreeNodes getCategoryDictTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode treeNode) {
		Object o;
		if (treeNode != null && (o = treeNode.getDataObject()) instanceof IRole) {
			treeNode.setImage(RoleSelectUtils.icon_role(cp, (IRole) o));
		}
		return super.getCategoryTreenodes(cp, treeBean, treeNode);
	}

	@Override
	protected void onLoaded_dataBinding(final ComponentParameter cp,
			final Map<String, Object> dataBinding, final PageSelector selector, final IRole role) {
		if (role != null) {
			dataBinding.put("role_type", role.getRoleType());
			// 该字段不能编辑
			selector.disabledSelector = "#role_type";
		}
	}

	@Override
	protected void onSave_setProperties(final ComponentParameter cp, final IRole role,
			final boolean insert) {
		if (insert) {
			role.setRoleChartId(getRoleChart(cp).getId());
		}
		final String role_type = cp.getParameter("role_type");
		if (StringUtils.hasText(role_type)) {
			role.setRoleType(Convert.toEnum(ERoleType.class, role_type));
		}
	}

	@Override
	protected String[] getContextMenuKeys() {
		return new String[] { "Add", "Edit", "Delete", "-", "Refresh", "-", "Move" };
	}

	@Override
	public Map<String, Object> toJSON(final ComponentParameter cp) {
		final IRoleChart roleChart = getRoleChart(cp);
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='nav_arrow'>");
		if (roleChart.getDepartmentId() == null) {
			sb.append($m("RoleChartCategory.0")).append(SpanElement.NAV);
		} else {
			final IDepartment dept = context.getDepartmentService().getBean(
					roleChart.getDepartmentId());
			if (dept != null) {
				sb.append(dept.getText()).append(SpanElement.NAV);
			}
		}
		sb.append(roleChart.getText()).append(SpanElement.shortText("(" + roleChart.getName() + ")"));
		sb.append("</div>");
		return ((KVMap) super.toJSON(cp)).add("title", sb.toString());
	}

	@Override
	protected AbstractComponentBean categoryEdit_createPropEditor(final ComponentParameter cp) {
		final PropEditorBean editor = (PropEditorBean) super.categoryEdit_createPropEditor(cp);
		editor.getFormFields().add(
				2,
				new PropField($m("RoleCategory.2")).addComponents(new InputComp("role_type").setType(
						EInputCompType.select).setDefaultValue(ERoleType.normal, ERoleType.handle,
						ERoleType.script)));
		return editor;
	}

	@Override
	public KVMap categoryEdit_attri(final ComponentParameter cp) {
		return ((KVMap) super.categoryEdit_attri(cp)).add(window_title, $m("RoleCategory.3")).add(
				window_height, 320);
	}
}
