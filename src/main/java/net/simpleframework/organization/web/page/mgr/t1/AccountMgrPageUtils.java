package net.simpleframework.organization.web.page.mgr.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.Date;

import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.organization.web.page.attri.AccountStatPage;
import net.simpleframework.organization.web.page.mgr.AccountEditPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AccountMgrPageUtils {

	public static void addAccountComponents(final PageParameter pp, final String key) {
		// 添加账号
		AjaxRequestBean ajaxRequest = pp.addComponentBean(key + "_editPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(AccountEditPage.class));
		pp.addComponentBean(key + "_edit", WindowBean.class).setContentRef(ajaxRequest.getName())
				.setTitle($m("AccountMgrPage.8")).setHeight(500).setWidth(620);

		// 帐号信息
		ajaxRequest = pp.addComponentBean(key + "_accountPage", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(AccountStatPage.class));
		pp.addComponentBean(key + "_accountWin", WindowBean.class)
				.setContentRef(ajaxRequest.getName()).setTitle($m("AccountMgrPage.18")).setHeight(450)
				.setWidth(380);
	}

	public static void addAccountTblCols(final TablePagerBean tablePager) {
		tablePager
				.addColumn(
						new TablePagerColumn("name", $m("AccountMgrPage.1"), 120)
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("u.text", $m("AccountMgrPage.2"), 120)
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("u.departmentId", $m("AccountMgrPage.5"), 180).setFilter(
								false).setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("u.email", $m("AccountMgrPage.6"))
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("u.mobile", $m("AccountMgrPage.7"))
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("lastLoginDate", $m("AccountMgrPage.3"), 115)
								.setPropertyClass(Date.class))
				.addColumn(new TablePagerColumn("status", $m("AccountMgrPage.4"), 60).setFilter(false))
				.addColumn(TablePagerColumn.OPE().setWidth(125));
	}
}
