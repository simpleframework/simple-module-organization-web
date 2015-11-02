package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.organization.Account;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AccountStatPage extends AbstractAccountPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 工作列表窗口
		final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "AccountStatPage_binding_page",
				AccountBindingPage.class);
		addWindowBean(pp, "AccountStatPage_binding", ajaxRequest).setWidth(400).setHeight(180)
				.setTitle($m("AccountStatPage.13"));
	}

	@Override
	public KVMap createVariables(final PageParameter pp) {
		final KVMap kv = super.createVariables(pp);
		final Account account = getAccount(pp);
		kv.add("status", account.getStatus())
				.add("createDate", blank(Convert.toDateString(account.getCreateDate())))
				.add("lastLoginDate", blank(Convert.toDateString(account.getLastLoginDate())))
				.add("lastLoginIP", blank(account.getLastLoginIP()))
				.add("loginTimes", account.getLoginTimes())
				.add("onlineMillis", DateUtils.toDifferenceDate(account.getOnlineMillis()))
				.add("mdevid", account.getMdevid());
		final boolean mailbinding = account.isMailbinding();
		kv.add("mailbinding", bool(mailbinding)).add("mailbinding_act", binding(mailbinding));
		final boolean mobilebinding = account.isMobilebinding();
		kv.add("mobilebinding", bool(mobilebinding)).add("mobilebinding_act", binding(mobilebinding));
		return kv;
	}

	private LinkButton binding(final boolean binding) {
		return binding ? LinkButton.corner($m("AccountStatPage.14")) : LinkButton.corner(
				$m("AccountStatPage.13")).setOnclick("$Actions['AccountStatPage_binding']();");
	}

	private String bool(final boolean b) {
		return $m(b ? "AccountStatPage.9" : "AccountStatPage.10");
	}

	private String blank(final Object o) {
		final String r = Convert.toString(o);
		return StringUtils.hasText(r) ? r : "&nbsp;";
	}

	public static class AccountBindingPage extends FormTableRowTemplatePage {

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			return TableRows.of();
		}
	}
}
