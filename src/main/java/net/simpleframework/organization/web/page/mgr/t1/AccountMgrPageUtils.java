package net.simpleframework.organization.web.page.mgr.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.Date;

import net.simpleframework.common.ID;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AccountMgrPageUtils implements IOrganizationContextAware {

	public static TablePagerColumn TC_NAME() {
		return new TablePagerColumn("name", $m("AccountMgrPage.1"), 125)
				.setTextAlign(ETextAlign.left);
	}

	public static TablePagerColumn TC_TEXT() {
		return new TablePagerColumn("u.text", $m("AccountMgrPage.2"), 125)
				.setTextAlign(ETextAlign.left);
	}

	public static TablePagerColumn TC_EMAIL() {
		return new TablePagerColumn("u.email", $m("AccountMgrPage.6"), 125)
				.setTextAlign(ETextAlign.left);
	}

	public static TablePagerColumn TC_MOBILE() {
		return new TablePagerColumn("u.mobile", $m("AccountMgrPage.7"), 125)
				.setTextAlign(ETextAlign.left);
	}

	public static TablePagerColumn TC_LASTLOGINDATE() {
		return new TablePagerColumn("lastLoginDate", $m("AccountMgrPage.3"), 115)
				.setPropertyClass(Date.class);
	}

	public static TablePagerColumn TC_STATUS() {
		return new TablePagerColumn("status", $m("AccountMgrPage.4"), 45).setFilter(false);
	}

	public static String toDepartmentText(final ID deptId) {
		final Department dept = orgContext.getDepartmentService().getBean(deptId);
		return dept != null && dept.getDepartmentType() == EDepartmentType.department ? dept
				.getText() : SpanElement.grey999($m("AccountMgrPage.20")).toString();
	}
}
