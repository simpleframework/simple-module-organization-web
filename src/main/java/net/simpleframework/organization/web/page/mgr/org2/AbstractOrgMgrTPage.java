package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.module.common.web.page.AbstractMgrTPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ETabMatch;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.impl.OrganizationContext;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationUrlsFactory;
import net.simpleframework.organization.web.component.deptselect.DeptSelectBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractOrgMgrTPage extends AbstractMgrTPage implements
		IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		pp.addImportCSS(AbstractOrgMgrTPage.class, "/orgmgrt.css");

		addComponentBean(pp, "AbstractMgrTPage_orgSelect", DeptSelectBean.class)
				.setOrg(true)
				.setClearAction("false")
				.setJsSelectCallback(
						"location.href = location.href.addParameter('orgId=' + selects[0].id); return false;");
	}

	@Override
	public String getRole(final PageParameter pp) {
		return OrganizationContext.ROLE_ORGANIZATION_MANAGER;
	}

	static Department getOrg2(final PageParameter pp) {
		return orgContext.getDepartmentService().getBean(getPermissionOrg(pp).getId());
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		SpanElement oele;
		final PermissionDept org = getPermissionOrg(pp);
		if (org != null) {
			String txt = org.getText();
			final int nums = org.getUsers();
			if (nums > 0) {
				txt += " (" + nums + ")";
			}
			oele = new SpanElement(txt);
		} else {
			oele = new SpanElement($m("AbstractMgrTPage.0"));
		}
		final ElementList el = ElementList.of(oele.setClassName("org_txt"));
		if (pp.isLmanager()) {
			el.append(SpanElement.SPACE).append(
					new LinkElement($m("AbstractMgrTPage.1"))
							.setOnclick("$Actions['AbstractMgrTPage_orgSelect']();"));
		}
		return el;
	}

	protected static OrganizationUrlsFactory uFactory = ((IOrganizationWebContext) orgContext)
			.getUrlsFactory();

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		final OrganizationUrlsFactory urlsFactory = ((IOrganizationWebContext) orgContext)
				.getUrlsFactory();
		String params = null;
		final String orgid = pp.getParameter("orgId");
		if (StringUtils.hasText(orgid)) {
			params = "orgId=" + orgid;
		}
		return TabButtons.of(
				new TabButton($m("AbstractOrgMgrTPage.0")).setHref(urlsFactory.getUrl(pp,
						DepartmentMgrTPage.class, params)),
				new TabButton($m("AbstractOrgMgrTPage.1")).setHref(
						urlsFactory.getUrl(pp, UserMgrTPage.class, params)).setTabMatch(
						ETabMatch.url_contains), new TabButton($m("AbstractOrgMgrTPage.2"))
						.setHref(urlsFactory.getUrl(pp, RoleMgrTPage.class, params)));
	}
}
