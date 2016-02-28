package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.common.MValidateCode;
import net.simpleframework.ctx.common.MValidateCode.Code;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.msg.IMessageContextAware;
import net.simpleframework.module.msg.ISMSService;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.EWarnType;
import net.simpleframework.mvc.component.base.validation.ValidationBean;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AccountStatPage extends AbstractAccountPage implements IMessageContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 邮件binding
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "AccountStatPage_mailbinding_page",
				AccountMailBindingPage.class);
		addWindowBean(pp, "AccountStatPage_mailbinding", ajaxRequest).setWidth(420).setHeight(250);

		// 手机binding
		ajaxRequest = addAjaxRequest(pp, "AccountStatPage_mobilebinding_page",
				AccountMobileBindingPage.class);
		addWindowBean(pp, "AccountStatPage_mobilebinding", ajaxRequest).setWidth(420).setHeight(250);
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
		return LinkButton.corner(binding ? $m("AccountStatPage.13") : $m("AccountStatPage.12"))
				.setOnclick(
						"$Actions['AccountStatPage_" + act + "']('accountId=" + account.getId()
								+ "&unbinding=" + binding + "');");
	}

	private String blank(final Object o) {
		final String r = Convert.toString(o);
		return StringUtils.hasText(r) ? r : "&nbsp;";
	}

	public String toTitleHTML(final PageParameter pp) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='tt'>");
		sb.append(" <strong>#(AccountStatPage.10)</strong>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class AccountMailBindingPage extends AbstractAccountBindingPage {
		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addFormValidationBean(pp).addValidators(
					new Validator(EValidatorMethod.required, "#mail_binding"),
					new Validator(EValidatorMethod.email, "#mail_binding"),
					new Validator(EValidatorMethod.required, "#mail_validate_code"));

			addComponentBean(pp, "AccountMailBindingPage_sent_validation", ValidationBean.class)
					.setWarnType(EWarnType.insertAfter)
					.setTriggerSelector("#mail_binding_btn")
					.addValidators(new Validator(EValidatorMethod.required, "#mail_binding"),
							new Validator(EValidatorMethod.email, "#mail_binding"));

			addAjaxRequest(pp, "AccountMailBindingPage_sentcode").setHandlerMethod("doSentcode");
		}

		public IForward doSentcode(final ComponentParameter cp) {
			return null;
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

			final boolean unbinding = pp.getBoolParameter("unbinding");
			final InputElement _unbinding = InputElement.hidden("unbinding").setVal(unbinding);
			final InputElement mail_binding = new InputElement("mail_binding").setReadonly(unbinding)
					.setPlaceholder($m("AccountEditPage.23")).setText(user.getEmail());
			final ButtonElement mail_binding_btn = new ButtonElement($m("AccountEditPage.20")).setId(
					"mail_binding_btn").setOnclick("AccountStatPage.mail_sent(this);");

			final InputElement mail_validate_code = new InputElement("mail_validate_code")
					.setPlaceholder($m("AccountEditPage.22"));

			return TableRows.of(new TableRow(new RowField($m("AccountEditPage.4"), _unbinding,
					mail_binding, mail_binding_btn)), new TableRow(new RowField(
					$m("AccountEditPage.19"), mail_validate_code)));
		}
	}

	public static class AccountMobileBindingPage extends AbstractAccountBindingPage {
		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addFormValidationBean(pp).addValidators(
					new Validator(EValidatorMethod.required, "#mobile_binding"),
					new Validator(EValidatorMethod.mobile_phone, "#mobile_binding"),
					new Validator(EValidatorMethod.required, "#mobile_validate_code"));

			addComponentBean(pp, "AccountMobileBindingPage_sent_validation", ValidationBean.class)
					.setWarnType(EWarnType.insertAfter)
					.setTriggerSelector("#mobile_binding_btn")
					.addValidators(new Validator(EValidatorMethod.required, "#mobile_binding"),
							new Validator(EValidatorMethod.mobile_phone, "#mobile_binding"));

			addAjaxRequest(pp, "AccountMobileBindingPage_sentcode").setHandlerMethod("doSentcode");
		}

		public IForward doSentcode(final ComponentParameter cp) {
			final String mobile = cp.getParameter("mobile");
			final ISMSService smsService = messageContext.getSMSService();
			final Code code = MValidateCode.genCode(mobile);
			smsService.sentSMS(mobile, "auth", new KVMap().add("code", code.val()).add("product", ""));
			return null;
		}

		@Transaction(context = IOrganizationContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final Account account = getAccount(cp);
			final User user = _accountService.getUser(account.getId());
			final String mobile = cp.getParameter("mobile_binding");
			MValidateCode.verifyCode(mobile, cp.getParameter("mobile_validate_code"));

			final boolean binding = !cp.getBoolParameter("unbinding");
			account.setMobilebinding(binding);
			_accountService.update(new String[] { "mobilebinding" }, account);
			if (binding && !mobile.equals(user.getMobile())) {
				user.setMobile(mobile);
				_userService.update(new String[] { "mobile" }, user);
			}

			return super.onSave(cp).append("$Actions.reloc();");
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final Account account = getAccount(pp);
			final User user = _accountService.getUser(account.getId());

			final boolean unbinding = pp.getBoolParameter("unbinding");
			final InputElement _unbinding = InputElement.hidden("unbinding").setVal(unbinding);
			final InputElement mobile_binding = new InputElement("mobile_binding")
					.setReadonly(unbinding).setPlaceholder($m("AccountEditPage.21"))
					.setText(user.getMobile());
			final ButtonElement mobile_binding_btn = new ButtonElement($m("AccountEditPage.20"))
					.setId("mobile_binding_btn").setOnclick("AccountStatPage.sms_sent(this);");

			final InputElement mobile_validate_code = new InputElement("mobile_validate_code")
					.setPlaceholder($m("AccountEditPage.22"));

			return TableRows.of(new TableRow(new RowField($m("AccountEditPage.5"), _unbinding,
					mobile_binding, mobile_binding_btn)), new TableRow(new RowField(
					$m("AccountEditPage.19"), mobile_validate_code)));
		}
	}

	public static abstract class AbstractAccountBindingPage extends FormTableRowTemplatePage {

		@Override
		public String getTitle(final PageParameter pp) {
			return pp.getBoolParameter("unbinding") ? $m("AccountStatPage.13")
					: $m("AccountStatPage.12");
		}
	}
}
