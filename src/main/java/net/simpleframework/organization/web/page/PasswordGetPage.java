package net.simpleframework.organization.web.page;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.object.ObjectUtils;
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
import net.simpleframework.organization.Account;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.User;
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
				.setTriggerSelector("#pg_email_btn")
				.setWarnType(EWarnType.insertLast)
				.addValidators(new Validator(EValidatorMethod.required, "#pg_email"),
						new Validator(EValidatorMethod.email, "#pg_email"));

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
		if ("email".equals(t)) {
			final IModuleRef ref = ((IOrganizationWebContext) orgContext).getMessageRef();
			if (ref != null) {
				final User user = _userService.getUserByEmail(cp.getParameter("val"));
				if (user == null) {
					return new JavascriptForward("alert('").append($m("PasswordGetPage.6"))
							.append("');");
				}
				final Object id = user.getId();
				final String code = ObjectUtils.hashStr(cp);
				((OrganizationMessageWebRef) ref).doPasswordGetMessage(_userService.getAccount(id),
						code);
				cp.setSessionAttr("password_get_code", new Object[] { code, id });
				return new JavascriptForward("alert('").append($m("PasswordGetPage.9")).append("');");
			}
		} else if ("code".equals(t)) {
			final Object[] arr = (Object[]) cp.getSessionAttr("password_get_code");
			if (arr == null) {
				return new JavascriptForward("alert('").append($m("PasswordGetPage.7")).append("');");
			}
			if (!arr[0].equals(cp.getParameter("val"))) {
				return new JavascriptForward("alert('").append($m("PasswordGetPage.8")).append("');");
			}

			final Account account = _accountService.getBean(arr[1]);
			if (account != null) {
				final String password = ObjectUtils.hashStr(cp);
				account.setPassword(Account.encrypt(password));
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
