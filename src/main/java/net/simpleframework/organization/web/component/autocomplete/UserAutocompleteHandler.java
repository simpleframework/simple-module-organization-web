package net.simpleframework.organization.web.component.autocomplete;

import java.util.ArrayList;

import net.simpleframework.ado.EFilterRelation;
import net.simpleframework.ado.FilterItem;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.autocomplete.AbstractAutocompleteHandler;
import net.simpleframework.mvc.component.ui.autocomplete.AutocompleteData;
import net.simpleframework.organization.IAccount;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRole;
import net.simpleframework.organization.IRoleChart;
import net.simpleframework.organization.IRoleChartService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserAutocompleteHandler extends AbstractAutocompleteHandler implements
		IOrganizationContextAware {

	@Override
	public AutocompleteData getData(final ComponentParameter cp, final String val) {
		String nVal = val;
		final String sepChar = (String) cp.getBeanProperty("sepChar");
		if (StringUtils.hasText(sepChar)) {
			int p;
			if ((p = val.lastIndexOf(sepChar)) > -1) {
				nVal = val.substring(p + sepChar.length());
			}
		}
		nVal = nVal.trim();

		final AutocompleteData aData = new AutocompleteData();

		final ArrayList<String> al = new ArrayList<String>();
		if (nVal.length() > 0 && nVal.charAt(0) == '#') {
			final IRoleChartService rcService = context.getRoleChartService();
			nVal = nVal.substring(1);
			final String[] arr = StringUtils.split(nVal, ":");
			if (arr.length == 2) {
				final IRoleChart rChart = rcService.getRoleChartByName(arr[0]);
				if (rChart != null) {
					if (arr[0].indexOf(arr[1]) == -1) {
						aData.setVal(arr[1]);
					}
					IRole role;
					final IDataQuery<IRole> dq = context.getRoleService().queryRoles(rChart);
					while ((role = dq.next()) != null) {
						final String rn = role.getName();
						if (rn.indexOf(arr[1]) > -1) {
							al.add("#" + rChart.getName() + ":" + rn);
						}
					}
				}
			} else {
				aData.setVal(nVal);
				final IDataQuery<IRoleChart> dq = rcService.queryByParams(FilterItems
						.of(new FilterItem("name", EFilterRelation.like, nVal)));
				IRoleChart rChart;
				while ((rChart = dq.next()) != null) {
					al.add("#" + rChart.getName());
				}
			}
		} else {
			if (nVal.length() > 0) {
				aData.setVal(nVal);
				final IDataQuery<IAccount> dq = context.getAccountService().queryByParams(
						FilterItems.of(new FilterItem("name", EFilterRelation.like, nVal)));
				IAccount account;
				while ((account = dq.next()) != null) {
					al.add(account.getName());
				}
			}
		}
		return aData.setList(al.toArray());
	}
}
