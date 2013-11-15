package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import javax.servlet.http.HttpSession;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.organization.IAccount;
import net.simpleframework.organization.IAccountSession;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.LoginObject;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class HttpAccountSession implements IAccountSession, IOrganizationContextAware {

	private PageRequestResponse rRequest;

	private final HttpSession httpSession;

	public HttpAccountSession(final PageRequestResponse rRequest) {
		this.rRequest = rRequest;
		httpSession = rRequest.getSession();
	}

	public HttpAccountSession(final HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}

	public PageRequestResponse getrPageRequest() {
		return rRequest;
	}

	@Override
	public LoginObject getLogin() {
		return (LoginObject) httpSession.getAttribute(LOGIN_KEY);
	}

	@Override
	public void setLogin(final LoginObject login) {
		if (login == null) {
			logout();
		} else {
			httpSession.setAttribute(LOGIN_KEY, login);
			final IModuleRef ref = ((IOrganizationWebContext) context).getLogRef();
			if (ref != null) {
				login.setAttr(
						"logId",
						((OrganizationLogRef) ref).logLogin(login.getAccountId(), getRemoteAddr(),
								login.getDescription()));
			}
		}
	}

	@Override
	public LoginObject getAutoLogin() {
		IAccount login = null;
		final String pwd = HttpUtils.getCookie(rRequest.request, "_account_pwd");
		if (StringUtils.hasText(pwd) && getLogin() == null) {
			login = context.getAccountService().getAccountByName(
					HttpUtils.getCookie(rRequest.request, "_account_name"));
		}
		return login != null ? new LoginObject(login.getId())
				.setDescription($m("HttpAccountSession.0")) : null;
	}

	@Override
	public void logout() {
		final LoginObject login = (LoginObject) httpSession.getAttribute(LOGIN_KEY);
		if (login != null) {
			httpSession.removeAttribute(LOGIN_KEY);
			final IModuleRef ref = ((IOrganizationWebContext) context).getLogRef();
			if (ref != null) {
				((OrganizationLogRef) ref).logLogout(login.getAttr("logId"));
			}
		}
		if (rRequest.response != null) {
			HttpUtils.addCookie(rRequest.response, "_account_pwd", null);
		}
	}

	@Override
	public long getOnlineMillis() {
		return System.currentTimeMillis() - httpSession.getLastAccessedTime();
	}

	@Override
	public String getRemoteAddr() {
		return HttpUtils.getRemoteAddr(rRequest.request);
	}
}
