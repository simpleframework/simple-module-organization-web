package net.simpleframework.organization.web.component.autocomplete;

import java.util.ArrayList;

import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.organization.IRoleChartService;
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
	public Object[] getData(final ComponentParameter cp, final String val, final String val2) {
		String nVal = val2;
		if (nVal.length() > 0 && nVal.charAt(0) == '#') {
			final ArrayList<String> al = new ArrayList<String>();
			final IRoleChartService rcService = orgContext.getRoleChartService();
			nVal = nVal.substring(1);
			final String[] arr = StringUtils.split(nVal, ":");
			if (arr.length == 2) {
				final RoleChart rChart = rcService.getRoleChartByName(arr[0]);
				if (rChart != null) {
					Role role;
					final IDataQuery<Role> dq = orgContext.getRoleService().queryRoles(rChart);
					while ((role = dq.next()) != null) {
						final String rn = role.getName();
						if (rn.indexOf(arr[1]) > -1) {
							al.add("#" + rChart.getName() + ":" + rn);
						}
					}
				}
			} else {
				final IDataQuery<RoleChart> dq = rcService.queryByParams(FilterItems.of().addLike(
						"name", nVal));
				RoleChart rChart;
				while ((rChart = dq.next()) != null) {
					al.add("#" + rChart.getName());
				}
			}
			return al.toArray();
		}
		return super.getData(cp, val, val2);
	}
}
