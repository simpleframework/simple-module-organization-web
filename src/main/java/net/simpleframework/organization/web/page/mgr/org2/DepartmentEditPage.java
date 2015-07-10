package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.EElementEvent;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.ValidationBean;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ui.propeditor.EInputCompType;
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.template.lets.FormPropEditorTemplatePage;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.impl.OrganizationContext;
import net.simpleframework.organization.web.component.deptselect.DeptSelectBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DepartmentEditPage extends FormPropEditorTemplatePage implements
		IOrganizationContextAware {
	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 验证
		addFormValidationBean(pp);
		// 部门选取字典
		addComponentBean(pp, "DepartmentEditPage_deptSelect", DeptSelectBean.class)
				.setMultiple(false).setBindingId("category_parentId")
				.setBindingText("category_parentText");
	}

	@Override
	public String getRole(final PageParameter pp) {
		return OrganizationContext.ROLE_ORGANIZATION_MANAGER;
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
		} else {
			dept.setParentId(cp.getLogin().getDomainId());
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
		final Department org = AbstractOrgMgrTPage.getOrg2(pp);
		if (org == null) {
			return;
		}

		final PropField f1 = new PropField($m("category_edit.0")).addComponents(new InputComp(
				"category_id").setType(EInputCompType.hidden), new InputComp("category_text"));
		final PropField f2 = new PropField($m("category_edit.1")).addComponents(new InputComp(
				"category_name"));

		final PropField f3 = new PropField($m("category_edit.2")).addComponents(
				new InputComp("category_parentId").setType(EInputCompType.hidden),
				new InputComp("category_parentText")
						.setType(EInputCompType.textButton)
						.setAttributes("readonly")
						.addEvent(EElementEvent.click,
								"$Actions['DepartmentEditPage_deptSelect']('orgId=" + org.getId() + "');"));
		final PropField f4 = new PropField($m("Description")).addComponents(new InputComp(
				"category_description").setType(EInputCompType.textarea).setAttributes("rows:6"));
		propEditor.getFormFields().append(f1, f2, f3, f4);
	}
}