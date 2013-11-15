package net.simpleframework.organization.web.component.chartselect;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.common.xml.XmlElement;
import net.simpleframework.mvc.PageDocument;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleChartSelectBean extends DictionaryBean {
	private boolean showGlobalChart = true;

	public RoleChartSelectBean(final PageDocument pageDocument, final XmlElement xmlElement) {
		super(pageDocument, xmlElement);

		setTitle($m("RoleChartSelectBean.0"));
		setWidth(280);
		setHeight(360);
		setHandleClass(DefaultRoleChartSelectHandler.class);
	}

	public boolean isShowGlobalChart() {
		return showGlobalChart;
	}

	public void setShowGlobalChart(final boolean showGlobalChart) {
		this.showGlobalChart = showGlobalChart;
	}
}
