package net.simpleframework.organization.web.page2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ado.query.ListDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.EElementEvent;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.ValidationBean;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.propeditor.EInputCompType;
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.mvc.template.lets.FormPropEditorTemplatePage;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.ERoleType;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IRoleChartService;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleChart;
import net.simpleframework.organization.web.component.roleselect.DefaultRoleSelectHandler;
import net.simpleframework.organization.web.component.roleselect.RoleSelectBean;
import net.simpleframework.organization.web.page.mgr.AddMembersPage;
import net.simpleframework.organization.web.page.mgr.t1.RoleMembersPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleMgrTPage extends AbstractMgrTPage {
	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp, "RoleMgrTPage_tbl")
				.setPagerBarLayout(EPagerBarLayout.bottom).setPageItems(30)
				.setContainerId("idRoleMgrTPage_tbl").setHandlerClass(RoleTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("text", $m("RoleMgrTPage.0"), 210).setTextAlign(
								ETextAlign.left).setSort(false))
				.addColumn(
						new TablePagerColumn("name", $m("RoleMgrTPage.1"), 120).setTextAlign(
								ETextAlign.left).setSort(false))
				.addColumn(
						new TablePagerColumn("roletype", $m("RoleMgrTPage.2"), 90).setTextAlign(
								ETextAlign.left).setFilterSort(false))
				.addColumn(TablePagerColumn.DESCRIPTION())
				.addColumn(TablePagerColumn.OPE().setWidth(125));

		// 成员窗口
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "RoleMgrTPage_membersPage",
				_RoleMembersPage.class);
		addWindowBean(pp, "RoleMgrTPage_members", ajaxRequest).setTitle($m("RoleMgrTPage.5"))
				.setWidth(800).setHeight(480);

		// 添加角色
		ajaxRequest = addAjaxRequest(pp, "RoleMgrTPage_rolePage", RoleEditPage.class);
		addWindowBean(pp, "RoleMgrTPage_roleWin", ajaxRequest).setTitle($m("RoleMgrTPage.6"))
				.setWidth(340).setHeight(360);
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='RoleMgrTPage clearfix'>");
		sb.append(" <div class='lnav'>");
		sb.append("  <div class='lbl'>#(RoleMgrTPage.3)</div>");
		final Department org = getOrg(pp);
		if (org != null) {
			final IRoleChartService cService = orgContext.getRoleChartService();
			final RoleChart _chart = cService.getBean(pp.getParameter("chartId"));
			final IDataQuery<RoleChart> dq = cService.query(org);
			RoleChart chart;
			int i = 0;
			while ((chart = dq.next()) != null) {
				sb.append("<div class='litem");
				if ((i++ == 0 && (_chart == null || !_chart.getDepartmentId().equals(org.getId())))
						|| chart.equals(_chart)) {
					sb.append(" active");
				}
				sb.append("' onclick=\"location.href = location.href.addParameter('chartId=")
						.append(chart.getId()).append("');\">").append(chart.getText()).append("</div>");
			}
		}
		sb.append(" </div>");
		sb.append(" <div class='rtbl'>");
		sb.append("  <div class='tbar'>");
		sb.append(ElementList.of(LinkButton.addBtn()
				.setOnclick("$Actions['RoleMgrTPage_roleWin']();"), SpanElement.SPACE, LinkButton
				.deleteBtn()));
		sb.append("  </div>");
		sb.append("  <div id='idRoleMgrTPage_tbl'></div>");
		sb.append(" </div>");
		sb.append("</div>");
		return sb.toString();
	}

	private static RoleChart _getRoleChart(final PageParameter pp) {
		final Department org = getOrg(pp);
		RoleChart rchart = null;
		if (org != null) {
			final IRoleChartService cService = orgContext.getRoleChartService();
			rchart = cService.getBean(pp.getParameter("chartId"));
			if (rchart == null || !rchart.getDepartmentId().equals(org.getId())) {
				rchart = cService.query(org).next();
			}
		}
		return rchart;
	}

	public static class RoleTbl extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final RoleChart rchart = _getRoleChart(cp);
			if (rchart != null) {
				cp.addFormParameter("orgId", rchart.getDepartmentId());
				cp.addFormParameter("chartId", rchart.getId());
				return new ListDataQuery<Role>(list(rchart, null));
			}
			return null;
		}

		final IRoleService rService = orgContext.getRoleService();

		private List<Role> list(final RoleChart chart, final Role parent) {
			final List<Role> l = new ArrayList<Role>();

			final IDataQuery<Role> dq = parent == null ? rService.queryRoot(chart) : rService
					.queryChildren(parent);
			Role role;
			while ((role = dq.next()) != null) {
				l.add(role);
				role.setAttr("_margin", parent != null ? Convert.toInt(parent.getAttr("_margin")) + 1
						: 1);
				l.addAll(list(chart, role));
			}
			return l;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final Role role = (Role) dataObject;
			final KVMap data = new KVMap();
			final StringBuilder txt = new StringBuilder();
			for (int i = 0; i < Convert.toInt(role.getAttr("_margin")); i++) {
				txt.append(i == 0 ? "| -- " : " -- ");
			}
			txt.append(role.getText());
			data.add("text", txt.toString());
			data.add("name", rService.toUniqueName(role));
			data.add("roletype", role.getRoleType());
			data.add(TablePagerColumn.OPE, toOpeHTML(cp, role));
			return data;
		}

		protected String toOpeHTML(final ComponentParameter cp, final Role role) {
			final Object id = role.getId();
			final StringBuilder sb = new StringBuilder();
			sb.append(
					new ButtonElement($m("RoleMgrTPage.4"))
							.setOnclick("$Actions['RoleMgrTPage_members']('roleId=" + id + "');"))
					.append(SpanElement.SPACE)
					.append(
							ButtonElement.editBtn().setOnclick(
									"$Actions['RoleMgrTPage_roleWin']('roleId=" + id + "');"));
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			return sb.toString();
		}
	}

	public static class _RoleMembersPage extends RoleMembersPage {
		@Override
		public String getRole(final PageParameter pp) {
			return IOrganizationContext.ROLE_ORGANIZATION_MANAGER;
		}

		@Override
		protected Class<? extends AbstractTemplatePage> getAddMembersPageClass() {
			return _AddMembersPage.class;
		}
	}

	public static class _AddMembersPage extends AddMembersPage {
		@Override
		protected JavascriptForward toJavascriptForward(final ComponentParameter cp, final Role role) {
			return new JavascriptForward().append("$Actions['RoleMemberPage_tbl']();");
		}
	}

	public static class RoleEditPage extends FormPropEditorTemplatePage {
		@Override
		protected void onForward(final PageParameter pp) {
			super.onForward(pp);

			addFormValidationBean(pp);

			// 角色选取
			addComponentBean(pp, "RoleEditPage_roleSelect", RoleSelectBean.class)
					.setBindingId("category_parentId").setBindingText("category_parentText")
					.setHandlerClass(_RoleSelectDict.class);
		}

		@Override
		protected ValidationBean addFormValidationBean(final PageParameter pp) {
			return super.addFormValidationBean(pp).addValidators(
					new Validator(EValidatorMethod.required, "#category_name, #category_text"));
		}

		@Override
		public void onLoad(final PageParameter pp, final Map<String, Object> dataBinding,
				final PageSelector selector) {
			super.onLoad(pp, dataBinding, selector);
			final IRoleService rService = orgContext.getRoleService();
			final Role r = rService.getBean(pp.getParameter("roleId"));
			if (r != null) {
				dataBinding.put("category_id", r.getId());
				dataBinding.put("category_name", r.getName());
				dataBinding.put("category_text", r.getText());
				dataBinding.put("role_isUserRole", r.isUserRole());
				final Role parent = rService.getBean(r.getParentId());
				if (parent != null) {
					dataBinding.put("category_parentId", parent.getId());
					dataBinding.put("category_parentText", parent.getText());
				}
				dataBinding.put("category_description", r.getDescription());
			}
		}

		@Transaction(context = IOrganizationContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final IRoleService rService = orgContext.getRoleService();
			Role r = rService.getBean(cp.getParameter("roleId"));
			final boolean insert = r == null;
			if (insert) {
				r = rService.createBean();
			}
			r.setName(cp.getParameter("category_name"));
			r.setText(cp.getParameter("category_text"));
			r.setUserRole(cp.getBoolParameter("role_isUserRole"));
			final Role parent = rService.getBean(cp.getParameter("category_parentId"));
			if (parent != null) {
				r.setParentId(parent.getId());
			}
			r.setDescription(cp.getParameter("category_description"));
			if (insert) {
				rService.insert(r);
			} else {
				rService.update(r);
			}
			return super.onSave(cp).append("$Actions['RoleMgrTPage_tbl']();");
		}

		@Override
		protected void initPropEditor(final PageParameter pp, final PropEditorBean propEditor) {
			final PropField f1 = new PropField($m("category_edit.0")).addComponents(new InputComp(
					"category_id").setType(EInputCompType.hidden), new InputComp("category_text"));
			final PropField f2 = new PropField($m("category_edit.1")).addComponents(new InputComp(
					"category_name"));

			final PropField f3 = new PropField($m("RoleCategory.4")).addComponents(InputComp
					.checkbox("role_isUserRole"));
			final Role r = orgContext.getRoleService().getBean(pp.getParameter("roleId"));
			final PropField f4 = new PropField($m("RoleCategory.2"))
					.addComponents(r == null ? InputComp.select("role_type", ERoleType.class)
							: InputComp.label(r.getRoleType()));

			final RoleChart rchart = _getRoleChart(pp);
			final PropField f5 = new PropField($m("category_edit.2")).addComponents(
					new InputComp("category_parentId").setType(EInputCompType.hidden),
					new InputComp("category_parentText")
							.setType(EInputCompType.textButton)
							.setAttributes("readonly")
							.addEvent(
									EElementEvent.click,
									"$Actions['RoleEditPage_roleSelect']("
											+ (rchart != null ? "'chartId=" + rchart.getId() + "'" : "")
											+ ");"));
			final PropField f6 = new PropField($m("Description")).addComponents(new InputComp(
					"category_description").setType(EInputCompType.textarea).setAttributes("rows:5"));
			propEditor.getFormFields().append(f1, f2, f3, f4, f5, f6);
		}
	}

	public static class _RoleSelectDict extends DefaultRoleSelectHandler {
		@Override
		public RoleChart getRoleChart(final ComponentParameter cp) {
			return _getRoleChart(cp);
		}
	}
}