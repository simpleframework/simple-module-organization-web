package net.simpleframework.organization.web.page.mgr.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.include.PageIncludeBean;
import net.simpleframework.mvc.component.ext.category.CategoryBean;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.t1.T1ResizedLCTemplatePage;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.web.page.mgr.RoleCategory;
import net.simpleframework.organization.web.page.mgr.RoleChartCategory;
import net.simpleframework.organization.web.page.mgr.RoleChartCategory.DeptContextMenu;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/org/role/mgr")
public class RoleMgrPage extends T1ResizedLCTemplatePage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		pp.addImportCSS(RoleMgrPage.class, "/role_mgr.css");

		// 创建roleChart tree
		addComponentBean(pp, "roleChartCategory", CategoryBean.class).setDraggable(false)
				.setContainerId("category_" + hashId).setHandlerClass(RoleChartCategory.class);

		addComponentBean(pp, "roleChartCategory_DeptMenu", MenuBean.class).setHandlerClass(
				DeptContextMenu.class);

		// 创建role tree
		addComponentBean(pp, "roleCategory", CategoryBean.class).setContainerId("idRoleCategory")
				.setHandlerClass(RoleCategory.class);

		addComponentBean(pp, "roleMemberVal", PageIncludeBean.class).setPageUrl(
				url(RoleMemberPage.class)).setContainerId("idRoleMemberVal");
		addComponentBean(pp, "ajaxRoleMemberVal", AjaxRequestBean.class).setUrlForward(
				url(RoleMemberPage.class)).setUpdateContainerId("idRoleMemberVal");
	}

	@Override
	public String getRole(final PageParameter pp) {
		return orgContext.getManagerRole();
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String variable) throws IOException {
		if ("content_left".equals(variable)) {
			return "<div id='category_" + hashId + "' style='padding: 6px;'></div>";
		}
		return null;
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		return super.getNavigationBar(pp).append(new SpanElement($m("RoleMgrPage.0")));
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		return TabButtons.of(new TabButton($m("AccountMgrPage.0"), url(AccountMgrPage.class)),
				new TabButton($m("RoleMgrPage.0"), url(RoleMgrPage.class)));
	}
}
