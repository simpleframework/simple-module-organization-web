package net.simpleframework.organization.web.page.mgr;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.common.Convert;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.EInputType;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.common.element.TextButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.deptselect.DeptSelectBean;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.bean.Department;
import net.simpleframework.organization.web.page.AbstractAccountAttriPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AccountEditPage extends AbstractAccountAttriPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 部门字典
		addComponentBean(pp, "AccountEditPage_deptDict", DeptSelectBean.class)
				.setRefreshAction("false").setBindingId("ue_departmentId")
				.setBindingText("id_departmentText");
	}

	@Override
	@Transaction(context = IOrganizationContext.class)
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		super.onSave(cp);
		return toJavascriptForward(cp);
	}

	protected JavascriptForward toJavascriptForward(final PageParameter pp) {
		final JavascriptForward js = CategoryTableLCTemplatePage.createTableRefresh();
		if (Convert.toBool(pp.getParameter(OPT_NEXT))) {
			js.append(resetForm());
		} else {
			js.append("$Actions['AccountMgrPage_edit'].close();");
		}
		return js;
	}

	protected String resetForm() {
		final StringBuilder js = new StringBuilder();
		js.append("var _txt = $F('id_departmentText');");
		js.append("var _id = $F('ue_departmentId');");
		js.append("$('").append(getFormSelector()).append("').down('form').reset();");
		js.append("if (_txt != '') $('id_departmentText').value = _txt;");
		js.append("if (_id != '') $('ue_departmentId').value = _id;");
		js.append("$('ae_accountName').focus();");
		return js.toString();
	}

	@Override
	protected Account getAccount(final PageParameter pp) {
		return _accountService.getBean(pp.getParameter("accountId"));
	}

	@Override
	protected boolean isShowOptNext(final PageParameter pp) {
		return getAccount(pp) == null;
	}

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		final Department org = getOrg(pp);
		String dept_click = "$Actions['AccountEditPage_deptDict'](";
		if (org != null) {
			dept_click += "'orgId=" + org.getId() + "'";
		}
		dept_click += ");";
		final TableRow r2 = new TableRow(
				new RowField($m("AccountEditPage.2"),
						new InputElement("ae_password", EInputType.password)).setStarMark(true),
				new RowField($m("AccountEditPage.3"), new TextButton("id_departmentText")
						.setHiddenField("ue_departmentId").setOnclick(dept_click)));
		return TableRows.of(r1(pp), r2, r3(pp), r4(pp), r5(pp), r6(pp), r7(pp), r8(pp), r9(pp),
				r10(pp));
	}
}