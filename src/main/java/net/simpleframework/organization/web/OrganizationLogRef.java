package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.ado.bean.AbstractIdBean;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.module.log.LogRef;
import net.simpleframework.module.log.web.page.EntityUpdateLogPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class OrganizationLogRef extends LogRef implements IOrganizationContextAware {

	public void addLogComponent(final PageParameter pp, final Class<?> cls) {
		final String clsn = cls.getSimpleName();
		final AjaxRequestBean ajaxRequest = pp
				.addComponentBean(clsn + "_logPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(AccountLogPage.class));
		pp.addComponentBean(clsn + "_logWin", WindowBean.class).setContentRef(ajaxRequest.getName())
				.setHeight(600).setWidth(960);
	}

	public static class AccountLogPage extends EntityUpdateLogPage {

		@Override
		protected IDbBeanService<?> getBeanService() {
			return _accountService;
		}

		@Override
		public String getTitle(final PageParameter pp) {
			final StringBuilder sb = new StringBuilder();
			sb.append($m("AccountMgrPage.0")).append(" - ");
			sb.append(_accountService.getUser(((AbstractIdBean) getBean(pp)).getId()));
			return sb.toString();
		}
	}
}
