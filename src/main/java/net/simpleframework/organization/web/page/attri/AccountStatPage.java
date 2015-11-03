package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
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

		// 邮件binding
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "AccountStatPage_mailbinding_page",
				AccountMailBindingPage.class);
		addWindowBean(pp, "AccountStatPage_mailbinding", ajaxRequest).setWidth(400).setHeight(180)
				.setTitle($m("AccountStatPage.12"));

		// 手机binding
		ajaxRequest = addAjaxRequest(pp, "AccountStatPage_mobilebinding_page",
				AccountMobileBindingPage.class);
		addWindowBean(pp, "AccountStatPage_mobilebinding", ajaxRequest).setWidth(400).setHeight(180)
				.setTitle($m("AccountStatPage.12"));

		// unbinding
		addAjaxRequest(pp, "AccountStatPage_unmailbinding").setConfirmMessage(
				$m("AccountStatPage.14")).setHandlerMethod("doUnMailbinding");
		addAjaxRequest(pp, "AccountStatPage_unmobilebinding").setConfirmMessage(
				$m("AccountStatPage.14")).setHandlerMethod("doUnMobilebinding");
	}

	@Override
	public KVMap createVariables(final PageParameter pp) {
		final KVMap kv = super.createVariables(pp);
		final Account account = getAccount(pp);
		final User user = _accountService.getUser(account.getId());
		kv.add("status", account.getStatus())
				.add("createDate", blank(Convert.toDateString(account.getCreateDate())))
				.add("lastLoginDate", blank(Convert.toDateString(account.getLastLoginDate())))
				.add("lastLoginIP", blank(account.getLastLoginIP()))
				.add("loginTimes", account.getLoginTimes())
				.add("onlineMillis", DateUtils.toDifferenceDate(account.getOnlineMillis()))
				.add("mdevid", account.getMdevid());
		final boolean mailbinding = account.isMailbinding();
		kv.add("mailbinding", mailbinding ? user.getEmail() : $m("AccountStatPage.9")).add(
				"mailbinding_act", createBinding(account, "mailbinding", mailbinding));
		final boolean mobilebinding = account.isMobilebinding();
		kv.add("mobilebinding", mobilebinding ? user.getMobile() : $m("AccountStatPage.9")).add(
				"mobilebinding_act", createBinding(account, "mobilebinding", mobilebinding));
		return kv;
	}

	private LinkButton createBinding(final Account account, final String act, final boolean binding) {
		if (binding) {
			return LinkButton.corner($m("AccountStatPage.13")).setOnclick(
					"$Actions['AccountStatPage_un" + act + "']('accountId=" + account.getId() + "');");
		} else {
			return LinkButton.corner($m("AccountStatPage.12")).setOnclick(
					"$Actions['AccountStatPage_" + act + "']('accountId=" + account.getId() + "');");
		}
	}

	private String blank(final Object o) {
		final String r = Convert.toString(o);
		return StringUtils.hasText(r) ? r : "&nbsp;";
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doUnMailbinding(final ComponentParameter cp) {
		final Account account = getAccount(cp);
		account.setMailbinding(false);
		_accountService.update(new String[] { "mailbinding" }, account);
		return JavascriptForward.RELOC;
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doUnMobilebinding(final ComponentParameter cp) {
		final Account account = getAccount(cp);
		account.setMobilebinding(false);
		_accountService.update(new String[] { "mobilebinding" }, account);
		return JavascriptForward.RELOC;
	}

	public static class AccountMailBindingPage extends FormTableRowTemplatePage {
		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addFormValidationBean(pp).addValidators(
					new Validator(EValidatorMethod.required, "#mail_binding"),
					new Validator(EValidatorMethod.email, "#mail_binding"));
		}

		@Transaction(context = IOrganizationContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final Account account = getAccount(cp);
			final User user = _accountService.getUser(account.getId());
			account.setMailbinding(true);
			_accountService.update(new String[] { "mailbinding" }, account);
			user.setEmail(cp.getParameter("mail_binding"));
			_userService.update(new String[] { "email" }, user);
			return super.onSave(cp).append("$Actions.reloc();");
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final Account account = getAccount(pp);
			final User user = _accountService.getUser(account.getId());
			return TableRows.of(new TableRow(new RowField($m("AccountEditPage.4"), InputElement
					.hidden("accountId"), new InputElement("mail_binding").setText(user.getEmail()))));
		}
	}

	public static class AccountMobileBindingPage extends FormTableRowTemplatePage {
		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addFormValidationBean(pp).addValidators(
					new Validator(EValidatorMethod.required, "#mobile_binding"),
					new Validator(EValidatorMethod.mobile_phone, "#mobile_binding"));
		}

		@Transaction(context = IOrganizationContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final Account account = getAccount(cp);
			final User user = _accountService.getUser(account.getId());
			account.setMobilebinding(true);
			_accountService.update(new String[] { "mobilebinding" }, account);
			user.setMobile(cp.getParameter("mobile_binding"));
			_userService.update(new String[] { "mobile" }, user);
			return super.onSave(cp).append("$Actions.reloc();");
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final Account account = getAccount(pp);
			final User user = _accountService.getUser(account.getId());
			return TableRows
					.of(new TableRow(new RowField($m("AccountEditPage.5"), InputElement
							.hidden("accountId"), new InputElement("mobile_binding").setText(user
							.getMobile()))));
		}
	}
}
