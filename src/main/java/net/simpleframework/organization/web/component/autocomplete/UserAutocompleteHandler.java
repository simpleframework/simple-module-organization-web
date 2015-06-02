package net.simpleframework.organization.web.component.autocomplete;

import java.util.ArrayList;

import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.autocomplete.AbstractAutocompleteHandler;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserAutocompleteHandler extends AbstractAutocompleteHandler implements
		IOrganizationContextAware {

	@Override
	public Object[] getData(final ComponentParameter cp, final String val, final String val2) {
		final ArrayList<String> al = new ArrayList<String>();
		final IDataQuery<Account> dq = orgContext.getAccountService().queryByParams(
				FilterItems.of().addLike("name", val2));
		Account account;
		while ((account = dq.next()) != null) {
			al.add(account.getName());
		}
		return al.toArray();
	}
}
