package net.simpleframework.organization.web.component.autocomplete;

import java.util.Enumeration;

import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.autocomplete.AutocompleteData;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserRoleAutocompleteHandler extends UserAutocompleteHandler {

	@Override
	public Enumeration<AutocompleteData> getData(final ComponentParameter cp, final String val,
			final String val2) {
		String nVal = val2;
		if (nVal.length() > 0 && nVal.charAt(0) == '#') {
			nVal = nVal.substring(1);
			final String[] arr = StringUtils.split(nVal, ":");
			if (arr.length == 2) {
				final RoleChart rChart = _rolecService.getRoleChartByName(arr[0]);
				final IDataQuery<Role> dq = rChart != null ? _roleService.queryRoles(rChart) : null;
				return new Enumeration<AutocompleteData>() {
					String rn;

					@Override
					public boolean hasMoreElements() {
						Role role;
						return (dq != null && (role = dq.next()) != null)
								&& ((rn = role.getName()).indexOf(arr[1]) > -1);
					}

					@Override
					public AutocompleteData nextElement() {
						return new AutocompleteData("#" + rChart.getName() + ":" + rn);
					}
				};
			} else {
				final IDataQuery<RoleChart> dq = _rolecService.queryByParams(FilterItems.of().addLike(
						"name", nVal));
				return new Enumeration<AutocompleteData>() {
					RoleChart rChart;

					@Override
					public boolean hasMoreElements() {
						return (rChart = dq.next()) != null;
					}

					@Override
					public AutocompleteData nextElement() {
						return new AutocompleteData("#" + rChart.getName());
					}
				};
			}
		}
		return super.getData(cp, val, val2);
	}
}
