package net.simpleframework.organization.web.component.roleselect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ComponentUtils;
import net.simpleframework.organization.ERoleMark;
import net.simpleframework.organization.ERoleType;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleChartService;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleChart;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class RoleSelectUtils implements IOrganizationContextAware {

	public static final String BEAN_ID = "roleselect_@bid";

	public static ComponentParameter get(final PageRequestResponse rRequest) {
		return ComponentParameter.get(rRequest, BEAN_ID);
	}

	public static ComponentParameter get(final HttpServletRequest request,
			final HttpServletResponse response) {
		return ComponentParameter.get(request, response, BEAN_ID);
	}

	public static RoleChart getRoleChart(final ComponentParameter cp) {
		final IRoleChartService service = orgContext.getRoleChartService();
		RoleChart roleChart = service.getBean(cp.getParameter("chartId"));
		if (roleChart == null) {
			roleChart = service.getRoleChartByName((String) cp.getBeanProperty("defaultRoleChart"));
		}
		return roleChart != null ? roleChart : orgContext.getSystemChart();
	}

	public static String icon_role(final PageParameter pp, final Role role) {
		final String imgBase = ComponentUtils.getCssResourceHomePath(pp, RoleSelectBean.class)
				+ "/images/";
		String img;
		final ERoleType rt = role.getRoleType();
		if (rt == ERoleType.handle) {
			img = "role_handle";
		} else if (rt == ERoleType.script) {
			img = "role_script";
		} else {
			img = "role";
		}
		if (role.getRoleMark() == ERoleMark.builtIn) {
			img += "_lock";
		}
		return imgBase + img + ".png";
	}
}
