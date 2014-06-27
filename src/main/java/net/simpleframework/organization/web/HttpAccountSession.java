package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import javax.servlet.http.HttpSession;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.mvc.IMVCConst;
import net.simpleframework.mvc.IMVCContextVar;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.IAccountService;
import net.simpleframework.organization.IAccountSession;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.LoginObject;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class HttpAccountSession implements IAccountSession, IOrganizationContextAware, IMVCConst,
		IMVCContextVar {

	private PageRequestResponse rRequest;

	private final HttpSession httpSession;

	public HttpAccountSession(final PageRequestResponse rRequest) {
		this.rRequest = rRequest;
		httpSession = rRequest.getSession();
	}

	public HttpAccountSession(final HttpSession httpSession) {
		this.httpSession = mvcContext.createHttpSession(httpSession);
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}

	public PageRequestResponse getPageRequest() {
		return rRequest;
	}

	@Override
	public String getSessionId() {
		return httpSession.getId();
	}

	@Override
	public LoginObject getLogin() {
		LoginObject lObj = (LoginObject) httpSession.getAttribute(LOGIN_KEY);
		// 根据jsessionid自动登录
		if (lObj == null && rRequest != null && rRequest.getRequestAttr("_jsessionid_login") == null
				&& rRequest.isHttpRequest()) {
			String jsessionid = rRequest.getParameter(JSESSIONID);
			if (!StringUtils.hasText(jsessionid)) {
				final String url = HttpUtils.getRequestURI(rRequest.request);
				final int p = url.toLowerCase().indexOf(";jsessionid=");
				if (p > 0) {
					jsessionid = url.substring(p + 12);
				} else {
					jsessionid = rRequest.getCookie(JSESSIONID);
				}
			}
			if (StringUtils.hasText(jsessionid)) {
				final IAccountService aService = orgContext.getAccountService();
				final Account account = aService.getAccountBySessionid(jsessionid);
				if (account != null) {
					aService.setLogin(
							this,
							lObj = new LoginObject(account.getId())
									.setDescription($m("HttpAccountSession.1")));
				}
			}
			rRequest.setRequestAttr("_jsessionid_login", Boolean.TRUE);
		}
		return lObj;
	}

	@Override
	public void setLogin(final LoginObject login) {
		if (login == null) {
			logout();
		} else {
			httpSession.setAttribute(LOGIN_KEY, login);
			final IModuleRef ref = ((IOrganizationWebContext) orgContext).getLogRef();
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
		Account login = null;
		final String pwd = HttpUtils.getCookie(rRequest.request, "_account_pwd");
		if (StringUtils.hasText(pwd) && getLogin() == null) {
			login = orgContext.getAccountService().getAccountByName(
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
			final IModuleRef ref = ((IOrganizationWebContext) orgContext).getLogRef();
			if (ref != null) {
				((OrganizationLogRef) ref).logLogout(login.getAttr("logId"));
			}
		}
		if (rRequest != null && rRequest.response != null) {
			HttpUtils.addCookie(rRequest.response, "_account_pwd", null);
		}
	}

	@Override
	public long getOnlineMillis() {
		return System.currentTimeMillis() - httpSession.getLastAccessedTime();
	}

	@Override
	public String getRemoteAddr() {
		return rRequest.getRemoteAddr();
	}
}
