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
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.component.ui.propeditor.PropFields;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.ERoleType;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleChart;
import net.simpleframework.organization.web.component.chartselect.RoleChartSelectUtils;
import net.simpleframework.organization.web.component.roleselect.RoleSelectUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleCategory extends CategoryBeanAwareHandler<Role> implements
		IOrganizationContextAware {

	@Override
	protected IRoleService getBeanService() {
		return _roleService;
	}

	private RoleChart getRoleChart(final PageRequestResponse rRequest) {
		RoleChart rchart = OmgrUtils.getRoleChart(rRequest);
		if (rchart == null) {
			rchart = orgContext.getSystemChart();
		}
		return rchart;
	}

	@Override
	protected IDataQuery<?> categoryBeans(final ComponentParameter cp, final Object categoryId) {
		final IRoleService service = getBeanService();
		final Role parent = service.getBean(categoryId);
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
			final RoleChart chart = getRoleChart(cp);
			tn.setImage(RoleChartSelectUtils.icon_chart(cp, chart));
			tn.setAcceptdrop(true);
			nodes.add(tn);
			return nodes;
		} else {
			final Object o = parent.getDataObject();
			if (o instanceof Role) {
				final Role role = (Role) o;
				parent.setImage(RoleSelectUtils.getRoleIcon(cp, role));
				if (role.getRoleType() == ERoleType.normal) {
					final int count = role.getMembers();
					if (count > 0) {
						parent.setPostfixText("(" + count + ")");
					}
				}
				parent.setJsClickCallback("$Actions['RoleMgrPage_ajax_roleMember']('roleId=" + ((Role) o).getId()
						+ "');");
			}
			return super.getCategoryTreenodes(cp, treeBean, parent);
		}
	}

	@Override
	public TreeNodes getCategoryDictTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode treeNode) {
		Object o;
		if (treeNode != null && (o = treeNode.getDataObject()) instanceof Role) {
			treeNode.setImage(RoleSelectUtils.getRoleIcon(cp, (Role) o));
		}
		return super.getCategoryTreenodes(cp, treeBean, treeNode);
	}

	@Override
	protected void onLoaded_dataBinding(final ComponentParameter cp,
			final Map<String, Object> dataBinding, final PageSelector selector, final Role role) {
		if (role != null) {
			dataBinding.put("role_isUserRole", role.isUserRole());
		}
	}

	@Override
	protected void onSave_setProperties(final ComponentParameter cp, final Role role,
			final boolean insert) {
		if (insert) {
			role.setRoleChartId(getRoleChart(cp).getId());
		}
		final String role_type = cp.getParameter("role_type");
		if (StringUtils.hasText(role_type)) {
			role.setRoleType(Convert.toEnum(ERoleType.class, role_type));
		}
		role.setUserRole(cp.getBoolParameter("role_isUserRole"));
	}

	@Override
	protected String[] getContextMenuKeys() {
		return new String[] { "Add", "Edit", "Delete", "-", "Refresh", "-", "Move" };
	}

	@Override
	public Map<String, Object> toJSON(final ComponentParameter cp) {
		final RoleChart roleChart = getRoleChart(cp);
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='nav_arrow'>");
		if (roleChart.getOrgId() == null) {
			sb.append($m("RoleChartCategory.0")).append(SpanElement.NAV);
		} else {
			final Department dept = _deptService.getBean(roleChart.getOrgId());
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
		final PropFields fields = editor.getFormFields();
		fields.add(2, new PropField($m("RoleCategory.4")).addComponents(InputComp
				.checkbox("role_isUserRole")));

		final Role r = _roleService.getBean(cp.getParameter(PARAM_CATEGORY_ID));
		if (r == null) {
			fields.add(2, new PropField($m("RoleCategory.2")).addComponents(InputComp.select(
					"role_type", ERoleType.class)));
		} else {
			// 该字段不能编辑
			fields.add(2,
					new PropField($m("RoleCategory.2")).addComponents(InputComp.label(r.getRoleType())));
		}
		return editor;
	}

	@Override
	public KVMap categoryEdit_attri(final ComponentParameter cp) {
		return ((KVMap) super.categoryEdit_attri(cp)).add(window_title, $m("RoleCategory.3"))
				.add(window_height, 360).add(window_width, 340);
	}
}
