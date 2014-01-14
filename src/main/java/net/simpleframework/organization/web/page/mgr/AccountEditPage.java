package net.simpleframework.organization.web.page.mgr;

import net.simpleframework.common.Convert;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.web.component.deptselect.DeptSelectBean;
import net.simpleframework.organization.web.page.AbstractAccountAttriPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class AccountEditPage extends AbstractAccountAttriPage {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		addComponentBean(pp, "AccountEditPage_deptDict", DeptSelectBean.class).setBindingId(
				"ue_departmentId").setBindingText("id_departmentText");
	}

	@Override
	@Transaction(context = IOrganizationContext.class)
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		super.onSave(cp);
		final JavascriptForward js = CategoryTableLCTemplatePage.createTableRefresh();
		if (Convert.toBool(cp.getParameter(OPT_NEXT))) {
			js.append("$('").append(getFormSelector()).append("').down('form').reset();");
			js.append("$('ae_accountName').focus();");
		} else {
			js.append("$Actions['AccountMgrPage_edit'].close();");
		}
		return js;
	}

	@Override
	protected Account getAccount(final PageParameter pp) {
		return context.getAccountService().getBean(pp.getParameter("accountId"));
	}

	@Override
	protected boolean show_opt_next(final PageParameter pp) {
		return getAccount(pp) == null;
	}

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		return TableRows.of(r1, r2, r3, r4, r5, r6, r7, r8, r9);
	}
}