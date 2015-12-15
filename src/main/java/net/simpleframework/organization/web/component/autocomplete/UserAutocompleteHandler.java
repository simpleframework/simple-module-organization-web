package net.simpleframework.organization.web.component.autocomplete;

import java.util.ArrayList;
import java.util.List;

import net.simpleframework.ado.db.common.SQLValue;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
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
		final StringBuilder sql = new StringBuilder("select a.* from ")
				.append(_userService.getTablename()).append(" u left join ")
				.append(_accountService.getTablename()).append(" a on u.id=a.id where 1=1");
		final List<Object> params = new ArrayList<Object>();
		final ID domainId = cp.getLdept().getDomainId();
		if (domainId != null) {
			sql.append(" and u.orgid=?");
			params.add(domainId);
		}
		sql.append(" and a.name like '%").append(val2).append("%'");
		final IDataQuery<Account> dq = _accountService.getEntityManager().queryBeans(
				new SQLValue(sql, params.toArray()));
		final ArrayList<String> al = new ArrayList<String>();
		Account account;
		while ((account = dq.next()) != null) {
			al.add(account.getName());
		}
		return al.toArray();
	}
}
