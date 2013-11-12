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
import net.simpleframework.mvc.common.element.EInputType;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TextButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ui.calendar.CalendarBean;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.organization.IAccount;
import net.simpleframework.organization.IDepartment;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IUser;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public abstract class AbstractAccountAttriPage extends FormTableRowTemplatePage implements
		IOrganizationContextAware {

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		addComponentBean(pp, "cal_Birthday", CalendarBean.class);

		addFormValidationBean(pp)
				.addValidators(
						new Validator(EValidatorMethod.required,
								"#ae_accountName, #ae_password, #ue_text, #ue_email"))
				.addValidators(
						new Validator(EValidatorMethod.min_length, "#ae_accountName, #ue_text", "2"))
				.addValidators(new Validator(EValidatorMethod.email, "#ue_email, #ue_msn"))
				.addValidators(new Validator(EValidatorMethod.mobile_phone, "#ue_mobile"))
				.addValidators(new Validator(EValidatorMethod.phone, "#ue_homePhone, #ue_officePhone"))
				.addValidators(new Validator(EValidatorMethod.date, "#ue_birthday", "yyyy-MM-dd"));
	}

	@Override
	public JavascriptForward onSave(final ComponentParameter cp) {
		final KVMap userData = new KVMap();
		final Enumeration<?> e = cp.getParameterNames();
		while (e.hasMoreElements()) {
			final String k = (String) e.nextElement();
			if (k.startsWith("ue_")) {
				userData.put(k.substring(3), cp.getParameter(k));
			}
		}
		context.getAccountService()
				.doSave(cp.getParameter("ae_id"), cp.getParameter("ae_accountName"),
						cp.getParameter("ae_password"), null, null, userData);
		return super.onSave(cp);
	}

	protected IAccount getAccount(final PageParameter pp) {
		Object id = pp.getParameter("accountId");
		if (!StringUtils.hasObject(id)) {
			id = pp.getLoginId();
		}
		return context.getAccountService().getBean(id);
	}

	@Override
	public void onLoad(final PageParameter pp, final Map<String, Object> dataBinding,
			final PageSelector selector) {
		final IAccount account = getAccount(pp);
		IDepartment dept = null;
		if (account != null) {
			dataBinding.put("ae_id", account.getId());
			dataBinding.put("ae_accountName", account.getName());
			dataBinding.put("ae_password", account.getPassword());

			final IUser user = context.getAccountService().getUser(account.getId());
			dept = context.getDepartmentService().getBean(user.getDepartmentId());
			final Map<String, Object> kv = BeanUtils.toMap(user);
			for (final String k : kv.keySet()) {
				Object o = kv.get(k);
				if (o instanceof Date) {
					o = Convert.toDateString((Date) o, "yyyy-MM-dd");
				}
				dataBinding.put("ue_" + k, o);
			}

			selector.readonlySelector = "#ae_accountName";
		}
		if (dept == null) {
			dept = context.getDepartmentService().getBean(pp.getParameter("deptId"));
		}
		if (dept != null) {
			dataBinding.put("ue_departmentId", dept.getId());
			dataBinding.put("id_departmentText", dept.getText());
		}
	}

	protected final TableRow r1 = new TableRow(new RowField($m("AccountEditPage.0"),
			InputElement.hidden("ae_id"), new InputElement("ae_accountName")).setStarMark(true),
			new RowField($m("AccountEditPage.1"), new InputElement("ue_text")).setStarMark(true));

	protected final TableRow r2 = new TableRow(new RowField($m("AccountEditPage.2"),
			new InputElement("ae_password", EInputType.password)).setStarMark(true), new RowField(
			$m("AccountEditPage.3"), new TextButton("id_departmentText").setHiddenField(
					"ue_departmentId").setOnclick("$Actions['AccountEditPage_deptDict']();")));

	protected final TableRow r3 = new TableRow(new RowField($m("AccountEditPage.4"),
			new InputElement("ue_email")).setStarMark(true), new RowField($m("AccountEditPage.5"),
			new InputElement("ue_mobile")));

	protected final TableRow r4 = new TableRow(new RowField($m("AccountEditPage.6"), InputElement
			.select("ue_sex").addElements(new Option($m("AccountEditPage.16")),
					new Option($m("AccountEditPage.17")))), new RowField($m("AccountEditPage.7"),
			new CalendarInput("ue_birthday").setCalendarComponent("cal_Birthday")));

	protected final TableRow r5 = new TableRow(new RowField($m("AccountEditPage.8"),
			new InputElement("ue_hometown")), new RowField($m("AccountEditPage.9"), new InputElement(
			"ue_postcode")));

	protected final TableRow r6 = new TableRow(new RowField($m("AccountEditPage.10"),
			new InputElement("ue_homePhone")), new RowField($m("AccountEditPage.11"),
			new InputElement("ue_officePhone")));

	protected final TableRow r7 = new TableRow(new RowField($m("AccountEditPage.12"),
			new InputElement("ue_qq")), new RowField($m("AccountEditPage.13"), new InputElement(
			"ue_msn")));

	protected final TableRow r8 = new TableRow(new RowField($m("AccountEditPage.14"),
			new InputElement("ue_address")));

	protected final TableRow r9 = new TableRow(new RowField($m("AccountEditPage.15"),
			InputElement.textarea("ue_description")));
}
