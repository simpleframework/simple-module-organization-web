package net.simpleframework.organization.web.component.deptselect;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DeptSelectBean extends DictionaryBean {

	public DeptSelectBean() {
		setTitle($m("DeptSelectBean.0"));
		setWidth(280);
		setHeight(360);
		setHandlerClass(DefaultDeptSelectHandler.class);
	}
}
