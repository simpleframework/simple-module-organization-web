package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.User;

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
		kv.add("mailbinding", bool(mailbinding)).add("mailbinding_act",
				createBinding(account, "mail", mailbinding));
		final boolean mobilebinding = account.isMobilebinding();
		kv.add("mobilebinding", bool(mobilebinding)).add("mobilebinding_act",
				createBinding(account, "mobile", mobilebinding));
		return kv;
	}

	private LinkButton createBinding(final Account account, final String type, final boolean binding) {
		return binding ? LinkButton.corner($m("AccountStatPage.14")) : LinkButton.corner(
				$m("AccountStatPage.13")).setOnclick(
				"$Actions['AccountStatPage_binding']('type=" + type + "&accountId=" + account.getId()
						+ "');");
	}

	private String bool(final boolean b) {
		return $m(b ? "AccountStatPage.9" : "AccountStatPage.10");
	}

	private String blank(final Object o) {
		final String r = Convert.toString(o);
		return StringUtils.hasText(r) ? r : "&nbsp;";
	}

	public static class AccountBindingPage extends FormTableRowTemplatePage {

		@Transaction(context = IOrganizationContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			return super.onSave(cp);
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final Account account = getAccount(pp);
			final User user = _accountService.getUser(account.getId());
			TableRow r = null;
			final String type = pp.getParameter("type");
			if ("mail".equals(type)) {
				r = new TableRow(new RowField($m("AccountEditPage.4"),
						new InputElement("atxt_binding").setText(user.getEmail())));
			} else if ("mobile".equals(type)) {
				r = new TableRow(new RowField($m("AccountEditPage.5"),
						new InputElement("atxt_binding").setText(user.getMobile())));
			}
			return r != null ? TableRows.of(r) : null;
		}
	}
}
