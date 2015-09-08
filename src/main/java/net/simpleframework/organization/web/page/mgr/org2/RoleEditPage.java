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
import net.simpleframework.organization.ERoleType;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleChart;
import net.simpleframework.organization.impl.OrganizationContext;
import net.simpleframework.organization.web.component.roleselect.DefaultRoleSelectHandler;
import net.simpleframework.organization.web.component.roleselect.RoleSelectBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleEditPage extends FormPropEditorTemplatePage implements IOrganizationContextAware {
	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addFormValidationBean(pp);

		// 角色选取
		addComponentBean(pp, "RoleEditPage_roleSelect", RoleSelectBean.class)
				.setBindingId("category_parentId").setBindingText("category_parentText")
				.setHandlerClass(_RoleSelectDict.class);
	}

	@Override
	public String getPageRole(final PageParameter pp) {
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
		final Role r = _roleService.getBean(pp.getParameter("roleId"));
		if (r != null) {
			dataBinding.put("category_id", r.getId());
			dataBinding.put("category_name", r.getName());
			dataBinding.put("category_text", r.getText());
			dataBinding.put("role_isUserRole", r.isUserRole());
			final Role parent = _roleService.getBean(r.getParentId());
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
		Role r = _roleService.getBean(cp.getParameter("roleId"));
		final boolean insert = r == null;
		if (insert) {
			r = _roleService.createBean();
			final RoleChart rchart = _getRoleChart(cp);
			r.setRoleChartId(rchart.getId());
		}
		r.setName(cp.getParameter("category_name"));
		r.setText(cp.getParameter("category_text"));
		r.setUserRole(cp.getBoolParameter("role_isUserRole"));
		final Role parent = _roleService.getBean(cp.getParameter("category_parentId"));
		if (parent != null) {
			r.setParentId(parent.getId());
		}
		r.setDescription(cp.getParameter("category_description"));
		if (insert) {
			_roleService.insert(r);
		} else {
			_roleService.update(r);
		}
		return super.onSave(cp).append("$Actions['RoleMgrTPage_tbl']();");
	}

	@Override
	protected void initPropEditor(final PageParameter pp, final PropEditorBean propEditor) {
		final RoleChart rchart = _getRoleChart(pp);
		if (rchart == null) {
			return;
		}

		final PropField f1 = new PropField($m("category_edit.0")).addComponents(new InputComp(
				"chartId").setDefaultValue(rchart.getId()).setType(EInputCompType.hidden),
				new InputComp("category_id").setType(EInputCompType.hidden), new InputComp(
						"category_text"));
		final PropField f2 = new PropField($m("category_edit.1")).addComponents(new InputComp(
				"category_name"));

		final PropField f3 = new PropField($m("RoleCategory.4")).addComponents(InputComp
				.checkbox("role_isUserRole"));
		final Role r = _roleService.getBean(pp.getParameter("roleId"));
		final PropField f4 = new PropField($m("RoleCategory.2")).addComponents(r == null ? InputComp
				.select("role_type", ERoleType.class) : InputComp.label(r.getRoleType()));

		final PropField f5 = new PropField($m("category_edit.2")).addComponents(
				new InputComp("category_parentId").setType(EInputCompType.hidden),
				new InputComp("category_parentText")
						.setType(EInputCompType.textButton)
						.setAttributes("readonly")
						.addEvent(EElementEvent.click,
								"$Actions['RoleEditPage_roleSelect']('chartId=" + rchart.getId() + "');"));
		final PropField f6 = new PropField($m("Description")).addComponents(new InputComp(
				"category_description").setType(EInputCompType.textarea).setAttributes("rows:5"));
		propEditor.getFormFields().append(f1, f2, f3, f4, f5, f6);
	}

	private static RoleChart _getRoleChart(final PageParameter pp) {
		RoleChart rchart = _rolecService.getBean(pp.getParameter("chartId"));
		if (rchart == null) {
			final Role r = _roleService.getBean(pp.getParameter("roleId"));
			if (r != null) {
				rchart = _rolecService.getBean(r.getRoleChartId());
			}
		}
		return rchart;
	}

	public static class _RoleSelectDict extends DefaultRoleSelectHandler {
		@Override
		public RoleChart getRoleChart(final ComponentParameter cp) {
			return _getRoleChart(cp);
		}
	}
}
