package net.simpleframework.organization.web.page.attri;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractAccountPage extends AbstractTemplatePage implements
		IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(AbstractAccountPage.class, "/account_attri.css");
	}

	public String buildFormHidden(final PageParameter pp) {
		final StringBuilder sb = new StringBuilder();
		sb.append(InputElement.hidden().setName("accountId").setValue(pp));
		return sb.toString();
	}

	protected Account getAccount(final PageParameter pp) {
		Object id = pp.getParameter("accountId");
		if (!StringUtils.hasObject(id)) {
			id = pp.getLoginId();
		}
		return _accountService.getBean(id);
	}

	@Override
	public KVMap createVariables(final PageParameter pp) {
		final KVMap variables = (KVMap) super.createVariables(pp);
		final Account account = getAccount(pp);
		variables.put("account", account);
		// variables.put("user", client.accountMgr().getUser(account.getId()));
		return variables;
	}
}
