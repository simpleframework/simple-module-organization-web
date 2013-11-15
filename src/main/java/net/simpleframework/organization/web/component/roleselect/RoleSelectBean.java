package net.simpleframework.organization.web.component.roleselect;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.common.xml.XmlElement;
import net.simpleframework.mvc.PageDocument;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleSelectBean extends DictionaryBean {

	private String roleChartHandler;

	/**
	 * 缺省的视图，默认为全局视图
	 */
	private String defaultRoleChart;

	public RoleSelectBean(final PageDocument pageDocument, final XmlElement xmlElement) {
		super(pageDocument, xmlElement);
		setTitle($m("RoleSelectBean.0"));
		setWidth(280);
		setHeight(360);
		setHandleClass(DefaultRoleSelectHandler.class);
	}

	public String getDefaultRoleChart() {
		return defaultRoleChart;
	}

	public RoleSelectBean setDefaultRoleChart(final String defaultRoleChart) {
		this.defaultRoleChart = defaultRoleChart;
		return this;
	}

	public String getRoleChartHandler() {
		return roleChartHandler;
	}

	public RoleSelectBean setRoleChartHandler(final String roleChartHandler) {
		this.roleChartHandler = roleChartHandler;
		return this;
	}
}
