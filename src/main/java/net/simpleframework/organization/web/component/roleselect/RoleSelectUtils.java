package net.simpleframework.organization.web.component.roleselect;

import static net.simpleframework.common.I18n.$m;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ComponentUtils;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.Role.ERoleType;
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

	public static String toChartHTML(final ComponentParameter cp) {
		final RoleChart chart = getRoleChart(cp);
		if (chart != null) {
			return chart.getText() + " (" + chart.getName() + ")";
		}
		return $m("RoleSelectUtils.0");
	}

	static RoleChart getRoleChart(final ComponentParameter cp) {
		RoleChart rchart = ((IRoleSelectHandle) cp.getComponentHandler()).getRoleChart(cp);
		if (rchart != null) {
			return rchart;
		}
		rchart = _rolecService.getBean(cp.getParameter("chartId"));
		if (rchart == null) {
			rchart = _rolecService.getRoleChartByName((String) cp.getBeanProperty("defaultRoleChart"));
		}
		if (rchart == null) {
			final Department org = _deptService.getBean(cp.getParameter("orgId"));
			if (org != null) {
				rchart = _rolecService.queryOrgCharts(org).next();
			} else {
				if (cp.isLmanager()) {
					rchart = orgContext.getSystemChart();
				}
			}
		}
		return rchart;
	}

	public static String getRoleIcon(final PageParameter pp, final Role role) {
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
		return imgBase + img + ".png";
	}
}
