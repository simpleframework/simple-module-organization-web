package net.simpleframework.organization.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import net.simpleframework.common.BeanUtils;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.CalendarInput;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.ValidationBean;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.bean.Department;
import net.simpleframework.organization.bean.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractAccountAttriPage extends FormTableRowTemplatePage
		implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addCalendarBean(pp, "cal_Birthday");

		addFormValidationBean(pp);
	}

	@Override
	public String getLabelWidth(final PageParameter pp) {
		return "85px";
	}

	@Override
	protected ValidationBean addFormValidationBean(final PageParameter pp) {
		return super.addFormValidationBean(pp)
				.addValidators(new Validator(EValidatorMethod.required,
						"#ae_accountName, #ae_password, #ue_text"))
				.addValidators(
						new Validator(EValidatorMethod.min_length, "#ae_accountName, #ue_text", "2"))
				// .addValidators(new Validator(EValidatorMethod.number,
				// "#ue_oorder"))
				.addValidators(new Validator(EValidatorMethod.email, "#ue_email"))
				.addValidators(new Validator(EValidatorMethod.mobile_phone, "#ue_mobile"))
				.addValidators(new Validator(EValidatorMethod.phone, "#ue_homePhone")) // ,
																												// #ue_officePhone
				.addValidators(new Validator(EValidatorMethod.date, "#ue_birthday", "yyyy-MM-dd"));
	}

	@Override
	public boolean isButtonsOnTop(final PageParameter pp) {
		return true;
	}

	protected Department getOrg(final PageParameter pp) {
		return null;
	}

	@Override
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		final KVMap userData = new KVMap();
		final Enumeration<?> e = cp.getParameterNames();
		while (e.hasMoreElements()) {
			final String k = (String) e.nextElement();
			if (k.startsWith("ue_")) {
				userData.put(k.substring(3), cp.getParameter(k));
			}
		}
		final Department org = getOrg(cp);
		if (org != null) {
			userData.put("orgId", org.getId());
		}

		_accountService.doSave(_accountService.getBean(cp.getParameter("ae_id")),
				cp.getParameter("ae_accountName"), cp.getParameter("ae_password"), null, userData);
		return super.onSave(cp);
	}

	protected Account getAccount(final PageParameter pp) {
		Object id = pp.getParameter("accountId");
		if (!StringUtils.hasObject(id)) {
			id = pp.getLoginId();
		}
		return _accountService.getBean(id);
	}

	@Override
	public void onLoad(final PageParameter pp, final Map<String, Object> dataBinding,
			final PageSelector selector) {
		final Account account = getAccount(pp);
		Department dept = null;
		if (account != null) {
			dataBinding.put("ae_id", account.getId());
			dataBinding.put("ae_accountName", account.getName());
			dataBinding.put("ae_password", account.getPassword());

			final User user = _accountService.getUser(account.getId());
			dept = _deptService.getBean(user.getDepartmentId());
			final Map<String, Object> kv = BeanUtils.toMap(user);
			for (final String k : kv.keySet()) {
				Object o = kv.get(k);
				if (o instanceof Date) {
					o = Convert.toDateString((Date) o, "yyyy-MM-dd");
				}
				dataBinding.put("ue_" + k, o);
			}
		}
		if (dept == null) {
			dept = _deptService.getBean(pp.getParameter("deptId"));
		}
		if (dept != null) {
			dataBinding.put("ue_departmentId", dept.getId());
			dataBinding.put("id_departmentText", dept.getText());
		}
	}

	protected void doR1(final PageParameter pp, final InputElement ae_accountName,
			final InputElement ue_text) {
	}

	protected final TableRow r1(final PageParameter pp) {
		final InputElement ae_accountName = new InputElement("ae_accountName");
		final InputElement ue_text = new InputElement("ue_text");
		doR1(pp, ae_accountName, ue_text);
		return new TableRow(
				new RowField($m("AccountEditPage.0"), InputElement.hidden("ae_id"), ae_accountName)
						.setStarMark(true),
				new RowField($m("AccountEditPage.1"), ue_text).setStarMark(true));
	}

	protected void doR3(final PageParameter pp, final InputElement ue_email,
			final InputElement ue_mobile) {
	}

	protected final TableRow r3(final PageParameter pp) {
		final InputElement ue_email = new InputElement("ue_email");
		final InputElement ue_mobile = new InputElement("ue_mobile");
		doR3(pp, ue_email, ue_mobile);
		return new TableRow(new RowField($m("AccountEditPage.4"), ue_email),
				new RowField($m("AccountEditPage.5"), ue_mobile));
	}

	protected final TableRow r4(final PageParameter pp) {
		return new TableRow(new RowField($m("AccountEditPage.18"), new InputElement("ue_mobile2")),
				new RowField($m("AccountEditPage.9"), new InputElement("ue_postcode")));
	}

	protected final TableRow r5(final PageParameter pp) {
		return new TableRow(
				new RowField($m("AccountEditPage.6"),
						InputElement.select("ue_sex").addElements(new Option($m("AccountEditPage.16")),
								new Option($m("AccountEditPage.17")))),
				new RowField($m("AccountEditPage.7"),
						new CalendarInput("ue_birthday").setCalendarComponent("cal_Birthday")));
	}

	protected final TableRow r6(final PageParameter pp) {
		return new TableRow(new RowField($m("AccountEditPage.10"), new InputElement("ue_homePhone")),
				new RowField($m("AccountEditPage.11"), new InputElement("ue_officePhone")));
	}

	protected final TableRow r7(final PageParameter pp) {
		return new TableRow(new RowField($m("AccountEditPage.12"), new InputElement("ue_nick")),
				new RowField($m("AccountEditPage.13"), new InputElement("ue_job")));
	}

	protected final TableRow r8(final PageParameter pp) {
		return new TableRow(new RowField($m("AccountEditPage.14"), new InputElement("ue_address")));
	}

	protected final TableRow r9(final PageParameter pp) {
		return new TableRow(new RowField($m("AccountEditPage.8"), new InputElement("ue_hometown")));
	}

	protected InputElement createTextarea() {
		return InputElement.textarea("ue_description").setRows(3);
	}

	protected final TableRow r10(final PageParameter pp) {
		return new TableRow(new RowField($m("AccountEditPage.15"), createTextarea()));
	}
}
