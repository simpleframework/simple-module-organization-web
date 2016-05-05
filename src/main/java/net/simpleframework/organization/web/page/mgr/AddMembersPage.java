package net.simpleframework.organization.web.page.mgr;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.EElementEvent;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.ValidationBean;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ext.deptselect.DeptSelectBean;
import net.simpleframework.mvc.component.ext.userselect.UserSelectBean;
import net.simpleframework.mvc.component.ui.propeditor.EInputCompType;
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.template.lets.FormPropEditorTemplatePage;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.bean.Department;
import net.simpleframework.organization.bean.Role;
import net.simpleframework.organization.bean.RoleChart;
import net.simpleframework.organization.bean.RoleMember;
import net.simpleframework.organization.bean.RoleMember.ERoleMemberType;
import net.simpleframework.organization.bean.User;
import net.simpleframework.organization.web.component.roleselect.RoleSelectBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AddMembersPage extends FormPropEditorTemplatePage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 验证
		addFormValidationBean(pp);

		// 用户选择字典
		addComponentBean(pp, "AddMembersPage_userSelect", UserSelectBean.class).setMultiple(true)
				.setBindingId("member_id").setBindingText("member_val");
		// 角色选择字典
		addComponentBean(pp, "AddMembersPage_roleSelect", RoleSelectBean.class).setBindingId(
				"member_id").setBindingText("member_val");
		addComponentBean(pp,
				new KVMap().add("name", "AddMembersPage_deptSelect").add("multiple", true),
				DeptSelectBean.class).setBindingId("member_id").setBindingText("member_val");

		// 部门选择
		addComponentBean(pp, "AddMembersPage_deptSelect2", DeptSelectBean.class).setBindingId(
				"member_deptId2").setBindingText("member_deptVal2");
	}

	@Override
	protected ValidationBean addFormValidationBean(final PageParameter pp) {
		return super.addFormValidationBean(pp).addValidators(
				new Validator(EValidatorMethod.required, "#member_val"));
	}

	@Override
	protected void initPropEditor(final PageParameter pp, final PropEditorBean propEditor) {
		final Role role = _roleService.getBean(pp.getParameter("roleId"));
		final RoleChart _chart = _rolecService.getBean(role.getRoleChartId());
		final Object orgId = _chart.getOrgId();

		final PropField f1 = new PropField($m("AddMembersPage.0")).addComponents(
				InputComp.hidden("roleId").setDefaultValue(String.valueOf(role.getId())),
				new InputComp("member_type").setType(EInputCompType.select).setDefaultEnumValue(
						ERoleMemberType.user, ERoleMemberType.role, ERoleMemberType.dept));

		final StringBuilder click = new StringBuilder();
		click.append("var mval = $F('member_type');");
		click.append("var params = 'orgId=").append(orgId).append("';");
		click.append("if (mval == '").append(ERoleMemberType.user.name()).append("') {");
		click.append(" $Actions['AddMembersPage_userSelect'](params);");
		click.append("} else if (mval == '").append(ERoleMemberType.role.name()).append("') {");
		click.append(" $Actions['AddMembersPage_roleSelect'](params);");
		click.append("} else {");
		click.append(" $Actions['AddMembersPage_deptSelect'](params);");
		click.append("}");
		final PropField f2 = new PropField($m("AddMembersPage.1")).addComponents(
				InputComp.hidden("member_id"),
				InputComp.textButton("member_val").addEvent(EElementEvent.click, click.toString()));

		final PropField f3 = new PropField($m("AddMembersPage.2")).addComponents(new InputComp(
				"member_primary").setType(EInputCompType.checkbox));

		final PropField f4 = new PropField($m("AddMembersPage.3")).addComponents(
				InputComp.hidden("member_deptId2"),
				InputComp.textButton("member_deptVal2").addEvent(
						EElementEvent.click,
						"if ($F('member_type') == '" + ERoleMemberType.user.name()
								+ "') $Actions['AddMembersPage_deptSelect2']('orgId=" + orgId
								+ "'); else alert('" + $m("AddMembersPage.4") + "');"));

		final PropField f5 = new PropField($m("Description")).addComponents(new InputComp(
				"member_description").setType(EInputCompType.textarea).setAttributes("rows:6"));

		propEditor.getFormFields().append(f1, f2, f3, f4, f5);
	}

	@Transaction(context = IOrganizationContext.class)
	@Override
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		final JavascriptForward js = super.onSave(cp);

		final Role role = _roleService.getBean(cp.getParameter("roleId"));
		final ERoleMemberType mType = Convert.toEnum(ERoleMemberType.class,
				cp.getParameter("member_type"));

		final boolean primary = Convert.toBool(cp.getParameter("member_primary"));
		final String deptId = cp.getParameter("member_deptId2");
		final String description = cp.getParameter("member_description");
		IDbBeanService<?> mgr;
		if (mType == ERoleMemberType.user) {
			mgr = _userService;
		} else if (mType == ERoleMemberType.role) {
			mgr = _roleService;
		} else {
			mgr = _deptService;
		}
		final ArrayList<RoleMember> beans = new ArrayList<RoleMember>();
		for (final String id : StringUtils.split(cp.getParameter("member_id"), ",")) {
			final IIdBeanAware bean = (IIdBeanAware) mgr.getBean(id);
			final ID mId = bean.getId();
			final RoleMember rm = _rolemService.createBean();
			rm.setRoleId(role.getId());
			rm.setMemberType(mType);
			rm.setMemberId(mId);
			if (mType == ERoleMemberType.user) {
				ID _deptId = null;
				if (StringUtils.hasText(deptId)) {
					final Department dept = _deptService.getBean(deptId);
					if (dept != null) {
						_deptId = dept.getId();
					}
				}
				rm.setDeptId(_deptId != null ? _deptId : ((User) bean).getDepartmentId());
			}
			rm.setPrimaryRole(primary);
			rm.setDescription(description);
			beans.add(rm);
		}

		_rolemService.insert(beans.toArray(new RoleMember[beans.size()]));

		return js.append(toJavascriptForward(cp, role));
	}

	protected JavascriptForward toJavascriptForward(final ComponentParameter cp, final Role role) {
		return new JavascriptForward("$Actions['RoleMgrPage_ajax_roleMember']('roleId=").append(
				role.getId()).append("')");
	}
}
