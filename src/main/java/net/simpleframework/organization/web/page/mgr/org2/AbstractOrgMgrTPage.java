package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.module.common.web.page.AbstractMgrTPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ETabMatch;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.ITablePagerHandler;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationUrlsFactory;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractOrgMgrTPage extends AbstractMgrTPage implements
		IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(AbstractOrgMgrTPage.class, "/orgmgrt.css");
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return PermissionConst.ROLE_DOMAIN_MANAGER;
	}

	@Override
	protected TablePagerBean addTablePagerBean(final PageParameter pp, final String name,
			final Class<? extends ITablePagerHandler> tblClass) {
		return (TablePagerBean) super.addTablePagerBean(pp, name, tblClass).setPagerBarLayout(
				EPagerBarLayout.bottom);
	}

	@Override
	protected LinkButton createOrgCancelBtn(final PageParameter pp) {
		return null;
	}

	static Department getOrg2(final PageParameter pp) {
		return _deptService.getBean(getPermissionOrg(pp).getId());
	}

	protected static OrganizationUrlsFactory uFactory = ((IOrganizationWebContext) orgContext)
			.getUrlsFactory();

	protected static String getUrl(final PageParameter pp,
			final Class<? extends AbstractOrgMgrTPage> pClass) {
		return getUrl(pp, pClass, null);
	}

	protected static String getUrl(final PageParameter pp,
			final Class<? extends AbstractOrgMgrTPage> pClass, final String params) {
		// final String orgid = pp.getParameter("orgId");
		// if (StringUtils.hasText(orgid)) {
		// if (params != null) {
		// params += "&orgId=" + orgid;
		// } else {
		// params = "orgId=" + orgid;
		// }
		// }
		return uFactory.getUrl(pp, pClass, params);
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		return TabButtons.of(new TabButton($m("AbstractOrgMgrTPage.0")).setHref(getUrl(pp,
				DepartmentMgrTPage.class)),
				new TabButton($m("AbstractOrgMgrTPage.1")).setHref(getUrl(pp, UserMgrTPage.class))
						.setTabMatch(ETabMatch.url_contains), new TabButton($m("AbstractOrgMgrTPage.2"))
						.setHref(getUrl(pp, RoleMgrTPage.class)).setTabMatch(ETabMatch.url_contains));
	}

	@Override
	protected SpanElement createOrgElement(final PageParameter pp) {
		final SpanElement ele = super.createOrgElement(pp);
		final PermissionDept org = getPermissionOrg(pp);
		if (org != null) {
			String txt = org.getText();
			final int nums = org.getUserCount(true);
			if (nums > 0) {
				txt += " (" + nums + ")";
			}
			ele.setText(txt);
		}
		return ele;
	}
}
