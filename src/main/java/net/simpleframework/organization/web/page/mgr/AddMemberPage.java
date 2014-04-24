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
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ext.userselect.UserSelectBean;
import net.simpleframework.mvc.component.ui.propeditor.EInputCompType;
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.template.lets.FormPropEditorTemplatePage;
import net.simpleframework.organization.ERoleMemberType;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleMemberService;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleMember;
import net.simpleframework.organization.web.component.roleselect.RoleSelectBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AddMemberPage extends FormPropEditorTemplatePage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		// 验证
		addFormValidationBean(pp).addValidators(
				new Validator(EValidatorMethod.required, "#member_val"));

		// 用户选择字典
		addComponentBean(pp, "dictUserSelect", UserSelectBean.class).setMultiple(true)
				.setBindingId("member_id").setBindingText("member_val");
		// 角色选择字典
		addComponentBean(pp, "dictRoleSelect", RoleSelectBean.class).setBindingId("member_id")
				.setBindingText("member_val");
	}

	@Override
	protected void initPropEditor(final PageParameter pp, final PropEditorBean propEditor) {
		final Role role = orgContext.getRoleService().getBean(pp.getParameter("roleId"));
		final PropField f1 = new PropField($m("AddMemberPage.0")).addComponents(
				new InputComp("roleId").setType(EInputCompType.hidden).setDefaultValue(
						String.valueOf(role.getId())),
				new InputComp("member_type").setType(EInputCompType.select).setDefaultValue(
						ERoleMemberType.user, ERoleMemberType.role));
		final PropField f2 = new PropField($m("AddMemberPage.1")).addComponents(
				new InputComp("member_id").setType(EInputCompType.hidden),
				new InputComp("member_val").setType(EInputCompType.textButton).addEvent(
						EElementEvent.click,
						"$Actions[$F('member_type') == '" + ERoleMemberType.user.name()
								+ "' ? 'dictUserSelect' : 'dictRoleSelect']();"));
		final PropField f3 = new PropField($m("AddMemberPage.2")).addComponents(new InputComp(
				"member_primary").setType(EInputCompType.checkbox));
		final PropField f4 = new PropField($m("Description")).addComponents(new InputComp(
				"member_description").setType(EInputCompType.textarea).setAttributes("rows:6"));
		propEditor.getFormFields().append(f1, f2, f3, f4);
	}

	@Transaction(context = IOrganizationContext.class)
	@Override
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		final IRoleService service = orgContext.getRoleService();

		final Role role = service.getBean(cp.getParameter("roleId"));
		final ERoleMemberType mType = Convert.toEnum(ERoleMemberType.class,
				cp.getParameter("member_type"));

		final boolean primary = Convert.toBool(cp.getParameter("member_primary"));
		final String description = cp.getParameter("member_description");
		final IDbBeanService<?> mgr = (mType == ERoleMemberType.user ? orgContext.getUserService()
				: service);
		final IRoleMemberService mService = orgContext.getRoleMemberService();
		final ArrayList<RoleMember> beans = new ArrayList<RoleMember>();
		for (final String id : StringUtils.split(cp.getParameter("member_id"), ",")) {
			final ID mId = ((IIdBeanAware) mgr.getBean(id)).getId();
			final RoleMember rm = mService.createBean();
			rm.setRoleId(role.getId());
			rm.setMemberType(mType);
			rm.setMemberId(mId);
			rm.setPrimaryRole(primary);
			rm.setDescription(description);
			beans.add(rm);
		}
		mService.insert(beans.toArray(new RoleMember[beans.size()]));

		return super.onSave(cp).append("$Actions['ajaxRoleMemberVal']('roleId=").append(role.getId())
				.append("')");
	}
}
