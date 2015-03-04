package net.simpleframework.organization.web.page2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ado.query.ListDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
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
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.propeditor.EInputCompType;
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.template.lets.FormPropEditorTemplatePage;
import net.simpleframework.organization.AccountStat;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IAccountStatService;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.web.component.deptselect.DeptSelectBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DepartmentMgrTPage extends AbstractMgrTPage {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp,
				"DepartmentMgrTPage_tbl").setShowFilterBar(false).setSort(false)
				.setPagerBarLayout(EPagerBarLayout.none).setContainerId("idDepartmentMgrTPage_tbl")
				.setHandlerClass(DepartmentTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("text", $m("DepartmentMgrTPage.0"))
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("name", $m("DepartmentMgrTPage.1"), 150)
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("parentId", $m("DepartmentMgrTPage.3"), 210)
								.setTextAlign(ETextAlign.left))
				.addColumn(TablePagerColumn.OPE().setWidth(80));

		// 添加部门
		final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "DepartmentMgrTPage_editPage",
				DepartmentEditPage.class);
		addWindowBean(pp, "DepartmentMgrTPage_editWin", ajaxRequest)
				.setTitle($m("DepartmentMgrTPage.2")).setHeight(320).setWidth(340);

		// 删除账号
		addDeleteAjaxRequest(pp, "DepartmentMgrTPage_delete");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		orgContext.getDepartmentService().delete(ids);
		return new JavascriptForward("$Actions['DepartmentMgrTPage_tbl']();");
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='tbar'>");
		sb.append(ElementList.of(LinkButton.addBtn().setOnclick(
				"$Actions['DepartmentMgrTPage_editWin']();")));
		sb.append("</div>");
		sb.append("<div id='idDepartmentMgrTPage_tbl'>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class DepartmentTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			return new ListDataQuery<Department>(list(getOrg(cp)));
		}

		private List<Department> list(final Department parent) {
			final List<Department> l = new ArrayList<Department>();
			if (parent != null) {
				final IDataQuery<Department> dq = orgContext.getDepartmentService().queryDepartments(
						parent, EDepartmentType.department);
				Department dept;
				while ((dept = dq.next()) != null) {
					l.add(dept);
					dept.setAttr("_margin", Convert.toInt(parent.getAttr("_margin")) + 1);
					l.addAll(list(dept));
				}
			}
			return l;
		}

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			if (menuItem != null) {
				return null;
			}
			final MenuItems items = MenuItems.of();
			items.add(MenuItem.itemDelete().setOnclick_act("DepartmentMgrTPage_delete", "id"));
			return items;
		}

		private final IAccountStatService sService = orgContext.getAccountStatService();
		private final IDepartmentService dService = orgContext.getDepartmentService();

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final Department dept = (Department) dataObject;
			final KVMap data = new KVMap();
			final StringBuilder txt = new StringBuilder();
			for (int i = 0; i < Convert.toInt(dept.getAttr("_margin")); i++) {
				txt.append(i == 0 ? "| --- " : " --- ");
			}
			txt.append(dept.getText());
			final AccountStat stat = sService.getDeptAccountStat(dept);
			final int nums = stat.getNums();
			if (nums > 0) {
				txt.append(" (").append(nums).append(")");
			}
			data.add("text", txt.toString());
			data.add("name", dept.getName());
			final Department parent = dService.getBean(dept.getParentId());
			if (parent != null && parent.getDepartmentType() == EDepartmentType.department) {
				data.add("parentId", SpanElement.grey999(parent.getText()));
			}
			data.add(TablePagerColumn.OPE, toOpeHTML(cp, dept));
			return data;
		}

		protected String toOpeHTML(final ComponentParameter cp, final Department dept) {
			final Object id = dept.getId();
			final StringBuilder sb = new StringBuilder();
			sb.append(ButtonElement.editBtn().setOnclick(
					"$Actions['DepartmentMgrTPage_editWin']('deptId=" + id + "');"));
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			return sb.toString();
		}
	}

	public static class DepartmentEditPage extends FormPropEditorTemplatePage {
		@Override
		protected void onForward(final PageParameter pp) {
			super.onForward(pp);

			// 验证
			addFormValidationBean(pp);

			// 部门选取字典
			addComponentBean(pp, "DepartmentEditPage_deptSelect", DeptSelectBean.class)
					.setMultiple(false).setBindingId("category_parentId")
					.setBindingText("category_parentText");
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
			final IDepartmentService dService = orgContext.getDepartmentService();
			final Department dept = dService.getBean(pp.getParameter("deptId"));
			if (dept != null) {
				dataBinding.put("category_id", dept.getId());
				dataBinding.put("category_name", dept.getName());
				dataBinding.put("category_text", dept.getText());
				final Department parent = dService.getBean(dept.getParentId());
				if (parent != null) {
					dataBinding.put("category_parentId", parent.getId());
					dataBinding.put("category_parentText", parent.getText());
				}
				dataBinding.put("category_description", dept.getDescription());
			}
		}

		@Transaction(context = IOrganizationContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final IDepartmentService dService = orgContext.getDepartmentService();
			Department dept = dService.getBean(cp.getParameter("category_id"));
			final boolean insert = dept == null;
			if (insert) {
				dept = dService.createBean();
			}
			dept.setName(cp.getParameter("category_name"));
			dept.setText(cp.getParameter("category_text"));
			final Department parent = dService.getBean(cp.getParameter("category_parentId"));
			if (parent != null) {
				dept.setParentId(parent.getId());
			}
			dept.setDescription(cp.getParameter("category_description"));
			if (insert) {
				dService.insert(dept);
			} else {
				dService.update(dept);
			}
			return super.onSave(cp).append("$Actions['DepartmentMgrTPage_tbl']();");
		}

		@Override
		protected void initPropEditor(final PageParameter pp, final PropEditorBean propEditor) {
			final PropField f1 = new PropField($m("category_edit.0")).addComponents(new InputComp(
					"category_id").setType(EInputCompType.hidden), new InputComp("category_text"));
			final PropField f2 = new PropField($m("category_edit.1")).addComponents(new InputComp(
					"category_name"));
			final Department org = getOrg(pp);
			final PropField f3 = new PropField($m("category_edit.2")).addComponents(
					new InputComp("category_parentId").setType(EInputCompType.hidden),
					new InputComp("category_parentText")
							.setType(EInputCompType.textButton)
							.setAttributes("readonly")
							.addEvent(
									EElementEvent.click,
									"$Actions['DepartmentEditPage_deptSelect']("
											+ (org != null ? "'orgId=" + org.getId() + "'" : "") + ");"));
			final PropField f4 = new PropField($m("Description")).addComponents(new InputComp(
					"category_description").setType(EInputCompType.textarea).setAttributes("rows:6"));
			propEditor.getFormFields().append(f1, f2, f3, f4);
		}
	}
}