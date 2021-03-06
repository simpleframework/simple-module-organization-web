package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectEx;
import net.simpleframework.common.object.ObjectFactory;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.mvc.MVCConst;
import net.simpleframework.mvc.MVCContext;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.bean.User;
import net.simpleframework.organization.login.IAccountSession;
import net.simpleframework.organization.login.LoginObject;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class HttpAccountSession extends ObjectEx
		implements IAccountSession, IOrganizationContextAware {

	private PageRequestResponse rRequest;

	private final HttpSession httpSession;

	public HttpAccountSession(final PageRequestResponse rRequest) {
		this.rRequest = rRequest;
		httpSession = rRequest.getSession();
	}

	public HttpAccountSession(final HttpSession httpSession) {
		this.httpSession = MVCContext.get().createHttpSession(httpSession);
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

	@SuppressWarnings("unchecked")
	public <T extends IHttpAccountListener> void addHttpAccountListener(final Class<T> lClass) {
		Set<Class<T>> listeners = (Set<Class<T>>) getHttpSession()
				.getAttribute("_httpaccount_listeners");
		if (listeners == null) {
			getHttpSession().setAttribute("_httpaccount_listeners", listeners = new LinkedHashSet<>());
		}
		listeners.add(lClass);
	}

	@SuppressWarnings("unchecked")
	private <T extends IHttpAccountListener> Set<Class<T>> getListeners() {
		return (Set<Class<T>>) getHttpSession().getAttribute("_httpaccount_listeners");
	}

	protected Object getSessionAttribute(final String key) {
		return httpSession.getAttribute(key);
	}

	protected void setSessionAttribute(final String key, final Object val) {
		httpSession.setAttribute(key, val);
	}

	protected void romoveSessionAttribute(final String key) {
		httpSession.removeAttribute(key);
	}

	protected String getLoginKey() {
		return LOGIN_KEY;
	}

	protected boolean isLogin(final Account account) {
		return true;
	}

	@Override
	public LoginObject getLogin() {
		LoginObject lObj = (LoginObject) getSessionAttribute(getLoginKey());
		// 根据jsessionid自动登录
		if (lObj == null && rRequest != null && rRequest.getRequestAttr("_jsessionid_login") == null
				&& rRequest.isHttpRequest()) {
			String jsessionid = rRequest.getParameter(MVCConst.JSESSIONID);
			if (!StringUtils.hasText(jsessionid)) {
				final String url = HttpUtils.getRequestURI(rRequest.request);
				final int p = url.toLowerCase().indexOf(";jsessionid=");
				if (p > 0) {
					jsessionid = url.substring(p + 12);
				} else {
					jsessionid = rRequest.getCookie(MVCConst.JSESSIONID);
				}
			}
			if (StringUtils.hasText(jsessionid)) {
				final Account account = _accountService.getAccountBySessionid(jsessionid);
				if (account != null && isLogin(account)) {
					_accountService.setLogin(this, lObj = new LoginObject(account.getId())
							.setDescription($m("HttpAccountSession.1")));

					oprintln("jsessionid: " + jsessionid);
					oprintln("auto login ok.");

					rRequest.setRequestAttr("_jsessionid_login", Boolean.TRUE);
				}
			}
		}
		return lObj;
	}

	@Override
	public void setLogin(final LoginObject login) {
		if (login == null) {
			logout();
		} else {
			setSessionAttribute(getLoginKey(), login);
			final IModuleRef ref = ((IOrganizationWebContext) orgContext).getLogRef();
			if (ref != null) {
				login.setAttr("logId", ((OrganizationLogRef) ref).logLogin(login.getAccountId(),
						getRemoteAddr(), login.getDescription()));
			}

			final Set<Class<IHttpAccountListener>> set = getListeners();
			if (set != null) {
				for (final Class<IHttpAccountListener> lClass : set) {
					ObjectFactory.singleton(lClass).login(this, login);
				}
			}
		}
	}

	@Override
	public LoginObject getAutoLogin() {
		Account login = null;
		final String pwd = HttpUtils.getCookie(rRequest.request, "_account_pwd");
		if (StringUtils.hasText(pwd) && getLogin() == null) {
			final String account_name = HttpUtils.getCookie(rRequest.request, "_account_name");
			if (StringUtils.hasText(account_name)) {
				login = _accountService.getAccountByName(account_name);
				if (login == null) {
					Account account = null;
					if (account_name.contains("@")) {
						final User user = _userService.getUserByEmail(account_name);
						if (user != null && (account = _userService.getAccount(user.getId())) != null
								&& account.isMailbinding()) {
							login = account;
						}
					} else {
						final User user = _userService.getUserByMobile(account_name);
						if (user != null && (account = _userService.getAccount(user.getId())) != null
								&& account.isMobilebinding()) {
							login = account;
						}
					}
				}
			}
		}
		return (login != null && isLogin(login))
				&& (login.getPassword().equals(pwd) || _accountService.verifyPassword(login, pwd))
						? new LoginObject(login.getId()).setDescription($m("HttpAccountSession.0"))
						: null;
	}

	@Override
	public void logout() {
		final String loginkey = getLoginKey();
		final LoginObject login = (LoginObject) getSessionAttribute(loginkey);
		if (login != null) {
			romoveSessionAttribute(loginkey);
			final IModuleRef ref = ((IOrganizationWebContext) orgContext).getLogRef();
			if (ref != null) {
				((OrganizationLogRef) ref).logLogout(login.getAttr("logId"));
			}
		}
		if (rRequest != null && rRequest.response != null) {
			HttpUtils.addCookie(rRequest.response, "_account_pwd", null);
		}

		final Set<Class<IHttpAccountListener>> set = getListeners();
		if (set != null) {
			for (final Class<IHttpAccountListener> lClass : set) {
				ObjectFactory.singleton(lClass).logout(this);
			}
		}
	}

	@Override
	public long getLastAccessedTime() {
		return httpSession.getLastAccessedTime();
	}

	@Override
	public String getRemoteAddr() {
		return rRequest.getRemoteAddr();
	}
}
