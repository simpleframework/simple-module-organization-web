package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.mail.Email;
import net.simpleframework.common.web.JavascriptUtils;
import net.simpleframework.ctx.common.MValidateCode;
import net.simpleframework.ctx.common.MValidateCode.Code;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.msg.IEmailService;
import net.simpleframework.module.msg.IMessageContextAware;
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
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.bean.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AccountStatFormPage extends AbstractAccountFormPage implements IMessageContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportJavascript(AccountStatFormPage.class, "/js/account-stat.js");

		// 邮件binding
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "AccountStatFormPage_mailbinding_page",
				AccountMailBindingPage.class);
		addWindowBean(pp, "AccountStatFormPage_mailbinding", ajaxRequest).setWidth(420)
				.setHeight(250);

		// 手机binding
		ajaxRequest = addAjaxRequest(pp, "AccountStatFormPage_mobilebinding_page",
				AccountMobileBindingPage.class);
		addWindowBean(pp, "AccountStatFormPage_mobilebinding", ajaxRequest).setWidth(420)
				.setHeight(250);
	}

	public String toTitleHTML(final PageParameter pp) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='tt'>");
		sb.append(" <strong>#(AccountStatFormPage.10)</strong>");
		sb.append("</div>");
		return sb.toString();
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		final Account account = getAccount(pp);
		final User user = _accountService.getUser(account.getId());
		sb.append("<div class='AccountStatFormPage'>");
		sb.append(toTitleHTML(pp));
		sb.append(" <table class='form_tbl'>");
		appendTrHTMLs(pp, sb, account, user);
		sb.append(" </table>");
		sb.append(" <div class='desc'>* #(AccountStatFormPage.14)</div>");
		sb.append("</div>");
		return sb.toString();
	}

	protected void appendTrHTMLs(final PageParameter pp, final StringBuilder sb,
			final Account account, final User user) {
		// 账号名
		sb.append(toTrHTML_name(pp, account));
		// 邮件绑定
		sb.append(toTrHTML_mailbinding(pp, account, user.getEmail()));
		// 手机绑定
		sb.append(toTrHTML_mobilebinding(pp, account, user.getMobile()));

		// 状态
		sb.append(toTrHTML(pp, $m("AccountStatFormPage.0"), account.getStatus()));
		// 创建时间
		sb.append(toTrHTML(pp, $m("AccountStatFormPage.1"),
				Convert.toDateTimeString(account.getCreateDate())));
		// 最后一次登录时间
		sb.append(toTrHTML(pp, $m("AccountStatFormPage.2"),
				Convert.toDateTimeString(account.getLastLoginDate())));
		// 最后一次登录IP
		sb.append(toTrHTML(pp, $m("AccountStatFormPage.3"), account.getLastLoginIP()));
		// 总登录次数
		sb.append(toTrHTML(pp, $m("AccountStatFormPage.4"), account.getLoginTimes()));
		// 总在线时间
		sb.append(toTrHTML(pp, $m("AccountStatFormPage.5"),
				DateUtils.toDifferenceDate(account.getOnlineMillis())));
		// 移动设备号
		sb.append(toTrHTML(pp, $m("AccountStatFormPage.11"), account.getMdevid()));
	}

	protected String toTrHTML_name(final PageParameter pp, final Account account) {
		return toTrHTML(pp, $m("AccountStatFormPage.16"), account.getName());
	}

	protected String toTrHTML_mailbinding(final PageParameter pp, final Account account,
			final String email) {
		final boolean mailbinding = account.isMailbinding();
		return toTrHTML(pp, $m("AccountStatFormPage.6"),
				mailbinding ? email : $m("AccountStatFormPage.9"),
				createBinding(account, "mailbinding", mailbinding));
	}

	protected String toTrHTML_mobilebinding(final PageParameter pp, final Account account,
			final String mobile) {
		final boolean mobilebinding = account.isMobilebinding();
		return toTrHTML(pp, $m("AccountStatFormPage.7"),
				mobilebinding ? mobile : $m("AccountStatFormPage.9"),
				createBinding(account, "mobilebinding", mobilebinding));
	}

	private LinkButton createBinding(final Account account, final String act,
			final boolean binding) {
		return LinkButton
				.corner(binding ? $m("AccountStatFormPage.13") : $m("AccountStatFormPage.12"))
				.setOnclick("$Actions['AccountStatFormPage_" + act + "']('accountId=" + account.getId()
						+ "&unbinding=" + binding + "');");
	}

	protected String toTrHTML(final PageParameter pp, final String lbl, final Object... vals) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<tr>");
		sb.append(" <td class='l'>").append(lbl).append("</td>");
		sb.append(" <td class='v'>");
		if (vals.length == 1) {
			sb.append(blank(vals[0]));
		}
		if (vals.length > 1) {
			sb.append("<div class='left'>").append(blank(vals[0])).append("</div>");
			sb.append("<div class='right'>").append(blank(vals[1])).append("</div>");
		}
		sb.append(" </td>");
		sb.append("</tr>");
		return sb.toString();
	}

	private String blank(final Object o) {
		final String r = Convert.toString(o);
		return StringUtils.hasText(r) ? r : "&nbsp;";
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
					.setWarnType(EWarnType.insertAfter).setTriggerSelector("#mail_binding_btn")
					.addValidators(new Validator(EValidatorMethod.required, "#mail_binding"),
							new Validator(EValidatorMethod.email, "#mail_binding"));

			addAjaxRequest(pp, "AccountMailBindingPage_sentcode").setHandlerMethod("doSentcode");
		}

		public IForward doSentcode(final ComponentParameter cp) {
			final String mail = cp.getParameter("mail");
			final IEmailService emailService = messageContext.getEmailService();
			final Code code = MValidateCode.genCode(mail);
			emailService
					.sentMail(Email.of(mail).subject($m("AccountStatFormPage.15")).addText(code.val()));
			return null;
		}

		@Transaction(context = IOrganizationContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final Account account = getAccount(cp);
			final User user = _accountService.getUser(account.getId());

			final String mail = cp.getParameter("mail_binding");
			MValidateCode.verifyCode(mail, cp.getParameter("mail_validate_code"));

			final boolean binding = !cp.getBoolParameter("unbinding");
			account.setMailbinding(binding);
			_accountService.update(new String[] { "mailbinding" }, account);

			user.setEmail(binding ? mail : "");
			_userService.update(new String[] { "email" }, user);
			return super.onSave(cp).append("$Actions.reloc();");
		}

		@Override
		public String toTableRowsString(final PageParameter pp) {
			final StringBuilder sb = new StringBuilder();
			sb.append(super.toTableRowsString(pp));
			sb.append(JavascriptUtils.wrapScriptTag("$UI.doMobileSentInterval('mail_binding_btn');"));
			return sb.toString();
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final Account account = getAccount(pp);
			final User user = _accountService.getUser(account.getId());

			final boolean unbinding = pp.getBoolParameter("unbinding");
			final InputElement _unbinding = InputElement.hidden("unbinding").setVal(unbinding);
			final String email = user.getEmail();
			final InputElement mail_binding = new InputElement("mail_binding")
					.setReadonly(unbinding && StringUtils.hasText(email))
					.setPlaceholder($m("AccountEditPage.23")).setText(email);
			final ButtonElement mail_binding_btn = new ButtonElement($m("AccountEditPage.20"))
					.setId("mail_binding_btn").setOnclick("AccountStatFormPage.mail_sent(this);");

			final InputElement mail_validate_code = new InputElement("mail_validate_code")
					.setPlaceholder($m("AccountEditPage.22"));

			return TableRows.of(
					new TableRow(new RowField($m("AccountEditPage.4"), _unbinding, mail_binding,
							mail_binding_btn)),
					new TableRow(new RowField($m("AccountEditPage.19"), mail_validate_code)));
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
					.setWarnType(EWarnType.insertAfter).setTriggerSelector("#mobile_binding_btn")
					.addValidators(new Validator(EValidatorMethod.required, "#mobile_binding"),
							new Validator(EValidatorMethod.mobile_phone, "#mobile_binding"));

			addAjaxRequest(pp, "AccountMobileBindingPage_sentcode").setHandlerMethod("doSentcode");
		}

		public IForward doSentcode(final ComponentParameter cp) {
			final String mobile = cp.getParameter("mobile");
			_accountService.sentBindingSMS(mobile);
			return null;
		}

		@Transaction(context = IOrganizationContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final Account account = getAccount(cp);
			final ID accountId = account.getId();
			final User user = _accountService.getUser(accountId);
			final String mobile = cp.getParameter("mobile_binding");
			MValidateCode.verifyCode(mobile, cp.getParameter("mobile_validate_code"));

			final boolean binding = !cp.getBoolParameter("unbinding");
			account.setMobilebinding(binding);
			_accountService.update(new String[] { "mobilebinding" }, account);

			user.setMobile(binding ? mobile : "");
			_userService.update(new String[] { "mobile" }, user);
			return super.onSave(cp).append("$Actions.reloc();");
		}

		@Override
		public String toTableRowsString(final PageParameter pp) {
			final StringBuilder sb = new StringBuilder();
			sb.append(super.toTableRowsString(pp));
			sb.append(
					JavascriptUtils.wrapScriptTag("$UI.doMobileSentInterval('mobile_binding_btn');"));
			return sb.toString();
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final Account account = getAccount(pp);
			final User user = _accountService.getUser(account.getId());

			final boolean unbinding = pp.getBoolParameter("unbinding");
			final InputElement _unbinding = InputElement.hidden("unbinding").setVal(unbinding);
			final String mobile = user.getMobile();
			final InputElement mobile_binding = new InputElement("mobile_binding")
					.setReadonly(unbinding && StringUtils.hasText(mobile))
					.setPlaceholder($m("AccountEditPage.21")).setText(mobile);
			final ButtonElement mobile_binding_btn = new ButtonElement($m("AccountEditPage.20"))
					.setId("mobile_binding_btn").setOnclick("AccountStatFormPage.sms_sent(this);");

			final InputElement mobile_validate_code = new InputElement("mobile_validate_code")
					.setPlaceholder($m("AccountEditPage.22"));

			return TableRows.of(
					new TableRow(new RowField($m("AccountEditPage.5"), _unbinding, mobile_binding,
							mobile_binding_btn)),
					new TableRow(new RowField($m("AccountEditPage.19"), mobile_validate_code)));
		}
	}

	public static abstract class AbstractAccountBindingPage extends FormTableRowTemplatePage {

		@Override
		public String getTitle(final PageParameter pp) {
			return pp.getBoolParameter("unbinding") ? $m("AccountStatFormPage.13")
					: $m("AccountStatFormPage.12");
		}
	}
}
