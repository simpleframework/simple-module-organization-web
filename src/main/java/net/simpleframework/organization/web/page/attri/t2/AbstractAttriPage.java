package net.simpleframework.organization.web.page.attri.t2;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.t2.T2TemplatePage;
import net.simpleframework.organization.web.page.attri.AbstractAttriTPage.AccountPasswordTPage;
import net.simpleframework.organization.web.page.attri.AbstractAttriTPage.AccountStatTPage;
import net.simpleframework.organization.web.page.attri.AbstractAttriTPage.PhotoTPage;
import net.simpleframework.organization.web.page.attri.AbstractAttriTPage.UserAttriTPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractAttriPage extends T2TemplatePage {

	protected abstract Class<? extends AbstractMVCPage> getAttriPage();

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='AbstractAttriPage'>");
		sb.append(pp.includeUrl(getAttriPage()));
		sb.append("</div>");
		return sb.toString();
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return PermissionConst.ROLE_ALL_ACCOUNT;
	}

	@PageMapping(url = "/my/user")
	public static class UserAttriPageT2 extends AbstractAttriPage {

		@Override
		protected Class<? extends AbstractMVCPage> getAttriPage() {
			return UserAttriTPage.class;
		}
	}

	@PageMapping(url = "/my/account")
	public static class AccountStatPageT2 extends AbstractAttriPage {

		@Override
		protected Class<? extends AbstractMVCPage> getAttriPage() {
			return AccountStatTPage.class;
		}
	}

	@PageMapping(url = "/my/password")
	public static class AccountPasswordPageT2 extends AbstractAttriPage {

		@Override
		protected Class<? extends AbstractMVCPage> getAttriPage() {
			return AccountPasswordTPage.class;
		}
	}

	@PageMapping(url = "/my/photo")
	public static class PhotoPageT2 extends AbstractAttriPage {

		@Override
		protected Class<? extends AbstractMVCPage> getAttriPage() {
			return PhotoTPage.class;
		}
	}
}
