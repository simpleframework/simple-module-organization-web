package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.module.log.LogRef;
import net.simpleframework.module.log.web.page.EntityUpdateLogPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class OrganizationLogRef extends LogRef implements IOrganizationContextAware {

	public void addLogComponent(final PageParameter pp) {
		pp.addComponentBean("AccountMgrPage_logPage", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(AccountLogPage.class));
		pp.addComponentBean("AccountMgrPage_logWin", WindowBean.class)
				.setContentRef("AccountMgrPage_logPage").setHeight(600).setWidth(960);
	}

	public static class AccountLogPage extends EntityUpdateLogPage {

		@Override
		protected Account getBean(final PageParameter pp) {
			return getCacheBean(pp, context.getAccountService(), getBeanIdParameter());
		}

		@Override
		public String getTitle(final PageParameter pp) {
			final StringBuilder sb = new StringBuilder();
			sb.append($m("AccountMgrPage.0")).append(" - ");
			sb.append(context.getAccountService().getUser(getBean(pp).getId()));
			return sb.toString();
		}
	}
}
