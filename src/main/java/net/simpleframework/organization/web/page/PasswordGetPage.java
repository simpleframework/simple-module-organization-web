package net.simpleframework.organization.web.page;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.object.ObjectUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.EWarnType;
import net.simpleframework.mvc.component.base.validation.ValidationBean;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.organization.IAccount;
import net.simpleframework.organization.IAccountService;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IUser;
import net.simpleframework.organization.IUserService;
import net.simpleframework.organization.impl.Account;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationMessageWebRef;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class PasswordGetPage extends AbstractTemplatePage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		addComponentBean(pp, "PasswordGetPage_validation", ValidationBean.class)
				.setTriggerSelector("#pg_email_btn")
				.setWarnType(EWarnType.insertLast)
				.addValidators(new Validator(EValidatorMethod.required, "#pg_email"),
						new Validator(EValidatorMethod.email, "#pg_email"));

		addComponentBean(pp, "PasswordGetPage_validation2", ValidationBean.class)
				.setTriggerSelector("#pg_code_btn").setWarnType(EWarnType.insertLast)
				.addValidators(new Validator(EValidatorMethod.required, "#pg_code"));

		addAjaxRequest(pp, "PasswordGetPage_post").setConfirmMessage($m("Confirm.Post"))
				.setHandleMethod("doPost");
	}

	public IForward doPost(final ComponentParameter cp) {
		final String t = cp.getParameter("t");
		if ("email".equals(t)) {
			final IModuleRef ref = ((IOrganizationWebContext) context).getMessageRef();
			if (ref != null) {
				final IUserService uService = context.getUserService();
				final IUser user = uService.getUserByMail(cp.getParameter("val"));
				if (user == null) {
					return new JavascriptForward("alert('").append($m("PasswordGetPage.6"))
							.append("');");
				}
				final Object id = user.getId();
				final String code = ObjectUtils.hashStr(cp);
				((OrganizationMessageWebRef) ref).doPasswordGetMessage(uService.getAccount(id), code);
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
			final IAccountService aService = context.getAccountService();
			final IAccount account = aService.getBean(arr[1]);
			if (account != null) {
				final String password = ObjectUtils.hashStr(cp);
				account.setPassword(Account.encrypt(password));
				aService.update(new String[] { "password" }, account);
				final JavascriptForward js = new JavascriptForward();
				js.append("var np=$('idNewPassword');");
				js.append("np.innerHTML='")
						.append($m("PasswordGetPage.10", account.getName(), password)).append("';");
				js.append("np.$shake();");
				return js;
			}
		}
		return null;
	}
}
