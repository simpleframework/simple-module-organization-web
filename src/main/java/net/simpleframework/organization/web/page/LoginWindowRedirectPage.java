package net.simpleframework.organization.web.page;

import java.util.Map;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ext.login.LoginBean;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class LoginWindowRedirectPage extends AbstractTemplatePage {

	static final String COMPONENT_PREFIX = "LoginWindowRedirectPage_";

	@Override
	public String getPageRole(final PageParameter pp) {
		return PermissionConst.ROLE_ANONYMOUS;
	}

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addComponentBean(COMPONENT_PREFIX + "Login", LoginBean.class)
				.setPasswordGetUrl(null)
				.setShowAccountType(false)
				.setShowResetAction(false)
				.setJsLoginCallback(
						"$Actions['" + LoginWindowRedirect.COMPONENT_PREFIX + "Window'].close();")
				.setContainerId("login_" + hashId);
	}

	@Override
	public Map<String, Object> createVariables(final PageParameter pp) {
		return ((KVMap) super.createVariables(pp)).add("loginId", "login_" + hashId);
	}
}
