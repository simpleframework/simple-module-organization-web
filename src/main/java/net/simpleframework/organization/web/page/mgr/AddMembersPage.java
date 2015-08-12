package net.simpleframework.organization.web.page.mgr;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
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
import net.simpleframework.organization.Department;
import net.simpleframework.organization.ERoleMemberType;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleMemberService;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleChart;
import net.simpleframework.organization.RoleMember;
import net.simpleframework.organization.User;
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
		addComponentBean(pp, "dictUserSelect", UserSelectBean.class).setMultiple(true)
				.setBindingId("member_id").setBindingText("member_val");
		// 角色选择字典
		addComponentBean(pp, "dictRoleSelect", RoleSelectBean.class).setBindingId("member_id")
				.setBindingText("member_val");
		// 部门选择
		addComponentBean(pp, "dictDeptSelect", DeptSelectBean.class).setBindingId("member_deptId")
				.setBindingText("member_deptVal");
	}

	@Override
	protected ValidationBean addFormValidationBean(final PageParameter pp) {
		return super.addFormValidationBean(pp).addValidators(
				new Validator(EValidatorMethod.required, "#member_val"));
	}

	@Override
	protected void initPropEditor(final PageParameter pp, final PropEditorBean propEditor) {
		final Role role = orgContext.getRoleService().getBean(pp.getParameter("roleId"));
		final RoleChart _chart = orgContext.getRoleChartService().getBean(role.getRoleChartId());
		final Object orgId = _chart.getOrgId();

		final PropField f1 = new PropField($m("AddMembersPage.0")).addComponents(
				new InputComp("roleId").setType(EInputCompType.hidden).setDefaultValue(
						String.valueOf(role.getId())),
				new InputComp("member_type").setType(EInputCompType.select).setDefaultEnumValue(
						ERoleMemberType.user, ERoleMemberType.role));

		final String utype = ERoleMemberType.user.name();
		final PropField f2 = new PropField($m("AddMembersPage.1")).addComponents(
				new InputComp("member_id").setType(EInputCompType.hidden),
				new InputComp("member_val").setType(EInputCompType.textButton).addEvent(
						EElementEvent.click,
						"$Actions[$F('member_type') == '" + utype
								+ "' ? 'dictUserSelect' : 'dictRoleSelect']('orgId=" + orgId + "');"));

		final PropField f3 = new PropField($m("AddMembersPage.2")).addComponents(new InputComp(
				"member_primary").setType(EInputCompType.checkbox));

		final PropField f4 = new PropField($m("AddMembersPage.3")).addComponents(
				new InputComp("member_deptId").setType(EInputCompType.hidden),
				new InputComp("member_deptVal").setType(EInputCompType.textButton).addEvent(
						EElementEvent.click,
						"if ($F('member_type') == '" + utype + "') $Actions['dictDeptSelect']('orgId="
								+ orgId + "'); else alert('" + $m("AddMembersPage.4") + "');"));

		final PropField f5 = new PropField($m("Description")).addComponents(new InputComp(
				"member_description").setType(EInputCompType.textarea).setAttributes("rows:6"));

		propEditor.getFormFields().append(f1, f2, f3, f4, f5);
	}

	@Transaction(context = IOrganizationContext.class)
	@Override
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		final JavascriptForward js = super.onSave(cp);

		final IRoleService service = orgContext.getRoleService();

		final Role role = service.getBean(cp.getParameter("roleId"));
		final ERoleMemberType mType = Convert.toEnum(ERoleMemberType.class,
				cp.getParameter("member_type"));

		final boolean primary = Convert.toBool(cp.getParameter("member_primary"));
		final String deptId = cp.getParameter("member_deptId");
		final String description = cp.getParameter("member_description");
		final IDbBeanService<?> mgr = (mType == ERoleMemberType.user ? _userService : service);
		final IRoleMemberService mService = orgContext.getRoleMemberService();
		final ArrayList<RoleMember> beans = new ArrayList<RoleMember>();
		for (final String id : StringUtils.split(cp.getParameter("member_id"), ",")) {
			final IIdBeanAware bean = (IIdBeanAware) mgr.getBean(id);
			final ID mId = bean.getId();
			final RoleMember rm = mService.createBean();
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

		mService.insert(beans.toArray(new RoleMember[beans.size()]));

		return js.append(toJavascriptForward(cp, role));
	}

	protected JavascriptForward toJavascriptForward(final ComponentParameter cp, final Role role) {
		return new JavascriptForward("$Actions['ajaxRoleMemberVal']('roleId=").append(role.getId())
				.append("')");
	}
}
