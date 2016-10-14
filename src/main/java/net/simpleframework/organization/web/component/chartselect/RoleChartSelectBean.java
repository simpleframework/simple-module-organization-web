package net.simpleframework.organization.web.component.chartselect;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleChartSelectBean extends DictionaryBean {
	private static final long serialVersionUID = 6981560487478694118L;

	private boolean showGlobalChart = true;

	public RoleChartSelectBean() {
		setTitle($m("RoleChartSelectBean.0"));
		setWidth(280);
		setHeight(360);
		setHandlerClass(DefaultRoleChartSelectHandler.class);
	}

	public boolean isShowGlobalChart() {
		return showGlobalChart;
	}

	public void setShowGlobalChart(final boolean showGlobalChart) {
		this.showGlobalChart = showGlobalChart;
	}
}
