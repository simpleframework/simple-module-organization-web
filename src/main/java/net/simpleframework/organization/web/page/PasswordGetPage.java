package net.simpleframework.organization.web.page;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.AlgorithmUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.EWarnType;
import net.simpleframework.mvc.component.base.validation.ValidationBean;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.bean.User;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationMessageWebRef;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class PasswordGetPage extends AbstractTemplatePage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(PasswordGetPage.class, "/pass_get.css");

		addComponentBean(pp, "PasswordGetPage_validation", ValidationBean.class)
				.setTriggerSelector("#pg_account_btn").setWarnType(EWarnType.insertLast)
				.addValidators(new Validator(EValidatorMethod.required, "#pg_account"));

		addComponentBean(pp, "PasswordGetPage_validation2", ValidationBean.class)
				.setTriggerSelector("#pg_code_btn").setWarnType(EWarnType.insertLast)
				.addValidators(new Validator(EValidatorMethod.required, "#pg_code"));

		addAjaxRequest(pp, "PasswordGetPage_post").setConfirmMessage($m("Confirm.Post"))
				.setHandlerMethod("doPost");
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return PermissionConst.ROLE_ANONYMOUS;
	}

	public IForward doPost(final ComponentParameter cp) {
		final String t = cp.getParameter("t");
		if ("account".equals(t)) {
			final IModuleRef ref = ((IOrganizationWebContext) orgContext).getMessageRef();
			String val;
			if (ref != null && StringUtils.hasText(val = cp.getParameter("val"))) {
				User user;
				final boolean email = val.contains("@");
				if (email) {
					user = _userService.getUserByEmail(val);
				} else {
					user = _userService.getUserByMobile(val);
				}
				if (user == null) {
					return JavascriptForward.alert($m("PasswordGetPage.6"));
				}

				final OrganizationMessageWebRef _ref = (OrganizationMessageWebRef) ref;
				final String code = StringUtils.genRandomNum(6);
				final Account account = _userService.getAccount(user.getId());
				if (email) {
					_ref.doPasswordResetEmailMessage(account, code);
				} else {
					_ref.doPasswordResetMobileMessage(account, code);
				}
				cp.setSessionAttr("password_get_code", new Object[] { code, account.getId() });
				return JavascriptForward.alert($m("PasswordGetPage.9"));
			}
		} else if ("code".equals(t)) {
			final Object[] arr = (Object[]) cp.getSessionAttr("password_get_code");
			if (arr == null) {
				return JavascriptForward.alert($m("PasswordGetPage.7"));
			}
			if (!arr[0].equals(cp.getParameter("val"))) {
				return JavascriptForward.alert($m("PasswordGetPage.8"));
			}

			final Account account = _accountService.getBean(arr[1]);
			if (account != null) {
				final String password = StringUtils.genRandomNum(8);
				account.setPassword(AlgorithmUtils.encryptPass(password));
				_accountService.update(new String[] { "password" }, account);

				final JavascriptForward js = new JavascriptForward();
				js.append("var np=$('idNewPassword');");
				js.append("np.innerHTML='")
						.append($m("PasswordGetPage.10", account.getName(), password)).append("';");
				js.append("$Effect.shake(np);");
				return js;
			}
		}
		return null;
	}
}
