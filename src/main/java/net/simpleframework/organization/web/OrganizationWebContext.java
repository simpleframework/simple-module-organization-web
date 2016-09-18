package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import net.simpleframework.ctx.IApplicationContext;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.ModuleFunctions;
import net.simpleframework.ctx.ModuleRefUtils;
import net.simpleframework.mvc.MVCContext;
import net.simpleframework.mvc.ctx.WebModuleFunction;
import net.simpleframework.organization.impl.OrganizationContext;
import net.simpleframework.organization.web.page.mgr.t1.AccountMgrPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class OrganizationWebContext extends OrganizationContext implements IOrganizationWebContext {

	@Override
	public void onInit(final IApplicationContext application) throws Exception {
		super.onInit(application);

		// 添加监听器
		MVCContext.get().getEventAdapter().addListener(new HttpSessionListener() {
			@Override
			public void sessionCreated(final HttpSessionEvent event) {
			}

			@Override
			public void sessionDestroyed(final HttpSessionEvent event) {
				doSessionDestroyed(event.getSession());
			}
		});
	}

	protected void doSessionDestroyed(final HttpSession httpSession) {
		// System.out.println("[sessionDestroyed] - ["
		// + (System.currentTimeMillis() - httpSession.getCreationTime()) / 1000 +
		// "s] - "
		// + httpSession.getId() + "");
		getAccountService().logout(new HttpAccountSession(httpSession), false);
	}

	// protected IOrganizationContext getRemoteContext() {
	// // 呼叫远程接口，目前调用本地
	// return this;
	// }
	//
	// @Override
	// public IAccountService getAccountMgr() {
	// return getRemoteContext().getAccountMgr();
	// }

	@Override
	public OrganizationUrlsFactory getUrlsFactory() {
		return singleton(OrganizationUrlsFactory.class);
	}

	@Override
	public IModuleRef getLogRef() {
		return ModuleRefUtils.getRef("net.simpleframework.organization.web.OrganizationLogRef");
	}

	@Override
	public IModuleRef getMessageRef() {
		return ModuleRefUtils
				.getRef("net.simpleframework.organization.web.OrganizationMessageWebRef");
	}

	@Override
	protected ModuleFunctions getFunctions() {
		return ModuleFunctions
				.of((WebModuleFunction) new WebModuleFunction(this, AccountMgrPage.class)
						.setName(MODULE_NAME + "-AccountMgrPage")
						.setText($m("OrganizationWebContext.0")));
	}
}
