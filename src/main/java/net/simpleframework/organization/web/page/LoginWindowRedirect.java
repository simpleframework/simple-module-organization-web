package net.simpleframework.organization.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class LoginWindowRedirect extends AbstractTemplatePage {

	static final String COMPONENT_PREFIX = "LoginWindowRedirect_";

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, COMPONENT_PREFIX + "Page",
				LoginWindowRedirectPage.class);
		addWindowBean(pp, COMPONENT_PREFIX + "Window", ajaxRequest).setWidth(420).setHeight(320)
				.setResizable(false).setTitle($m("LoginWindowRedirect.0"));
	}

	@Override
	public Map<String, Object> createVariables(final PageParameter pp) {
		return ((KVMap) super.createVariables(pp)).add("loginId", "login_" + hashId).add("win",
				COMPONENT_PREFIX + "Window");
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return PermissionConst.ROLE_ANONYMOUS;
	}
}
