package net.simpleframework.organization.web.component.login;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.login.AbstractLoginHandler;
import net.simpleframework.mvc.component.ui.validatecode.ValidateCodeUtils;
import net.simpleframework.organization.Account.EAccountType;
import net.simpleframework.organization.OrganizationException;
import net.simpleframework.organization.web.OrganizationPermissionHandler;
import net.simpleframework.organization.web.page.PasswordGetPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultLoginHandler extends AbstractLoginHandler {

	@Override
	public Object getBeanProperty(final ComponentParameter cp, final String beanProperty) {
		if ("passwordGetUrl".equals(beanProperty)) {
			return AbstractMVCPage.url(PasswordGetPage.class);
		}
		return super.getBeanProperty(cp, beanProperty);
	}

	@Override
	public IForward login(final ComponentParameter cp) {
		final JavascriptForward js = new JavascriptForward();

		final boolean showValidateCode = (Boolean) cp.getBeanProperty("showValidateCode");
		if (showValidateCode && !ValidateCodeUtils.isValidateCode(cp.request, "vcode")) {
			js.append("var code = $('#idLoginLoaded_vcode input');");
			js.append("Validation.insertAfter('#idLoginLoaded_vcode input', '")
					.append(ValidateCodeUtils.getErrorString()).append("');");
			js.append("$Effect.shake(code.next());");
			return js;
		}

		final String lastUrl = getLastUrl(cp);
		final String loginForward = StringUtils.hasText(lastUrl) ? lastUrl : (String) cp
				.getBeanProperty("loginForward");
		try {
			doLogin(cp);
			final String loginCallback = (String) cp.getBeanProperty("jsLoginCallback");
			if (StringUtils.hasText(loginCallback)) {
				js.append(loginCallback);
			} else {
				js.append(JS.loc(loginForward));
			}
			js.append("_save_cookie();");
		} catch (final OrganizationException e) {
			final int code = e.getCode();
			if (code == 2002) {
				js.append(JS.loc(loginForward));
			} else {
				final boolean password = Convert.toBool(e.getVal("password"));
				js.append("Validation.insertAfter('");
				js.append(password ? "_passwordName" : "_accountName").append("', '")
						.append(e.getMessage()).append("');");
			}
		}
		return js;
	}

	protected void doLogin(final PageRequestResponse rRequest) {
		rRequest.getPermission().login(
				rRequest,
				rRequest.getParameter("_accountName"),
				rRequest.getParameter("_passwordName"),
				new KVMap().add(OrganizationPermissionHandler.ACCOUNT_TYPE,
						Convert.toEnum(EAccountType.class, rRequest.getParameter("_accountType"))));
	}
}
