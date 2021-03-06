package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.common.AlgorithmUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.PwdStrength;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.EWarnType;
import net.simpleframework.mvc.component.base.validation.ValidationBean;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationMessageWebRef;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AccountPasswordFormPage extends AbstractAccountFormPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addComponentBean(pp, "userpwdValidation", ValidationBean.class)
				.setTriggerSelector("#_userpwd_save").setWarnType(EWarnType.insertAfter)
				.addValidators(new Validator(EValidatorMethod.required,
						"#user_old_password, #user_password, #user_password2"))
				.addValidators(new Validator(EValidatorMethod.equals, "#user_password2")
						.setArgs("#user_password"));

		addAjaxRequest(pp, "ajaxEditPassword").setConfirmMessage($m("Confirm.Post"))
				.setHandlerMethod("saveAction").setSelector(".AccountPasswordFormPage");
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return PermissionConst.ROLE_ALL_ACCOUNT;
	}

	@Override
	public KVMap createVariables(final PageParameter pp) {
		return super.createVariables(pp)
				.add("nav",
						BlockElement.nav()
								.addElements(SpanElement.strongText($m("AccountPasswordFormPage.0"))))
				.add("pwdstrength", new PwdStrength().setTextInput("user_password")).add("sendmail",
						new Checkbox("user_SendMail", $m("AccountPasswordFormPage.1")).setChecked(true));
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward saveAction(final ComponentParameter cp) {
		final JavascriptForward js = new JavascriptForward(
				"Validation.clearInsert(['user_old_password']);");
		final Account account = getAccount(cp);
		final String oldpassword = cp.getParameter("user_old_password");
		if (!_accountService.verifyPassword(account, oldpassword)) {
			js.append("Validation.insertAfter('user_old_password', '")
					.append($m("AccountPasswordFormPage.6")).append("');");
		} else {
			final String password = cp.getParameter("user_password");
			account.setPassword(AlgorithmUtils.encryptPass(password));
			_accountService.update(new String[] { "password" }, account);
			if (cp.getBoolParameter("user_SendMail")) {
				final IModuleRef ref = ((IOrganizationWebContext) orgContext).getMessageRef();
				if (ref != null) {
					((OrganizationMessageWebRef) ref).doPasswordEditMessage(account, password);
				}
			}
			js.append("alert($MessageConst['SaveOK']);");
		}
		return js;
	}
}
