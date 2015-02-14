package net.simpleframework.organization.web.page2;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.StringUtils;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.template.lets.Tabs_BlankPage;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationUrlsFactory;
import net.simpleframework.organization.web.component.deptselect.DeptSelectBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractMgrTPage extends Tabs_BlankPage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		pp.addImportCSS(AbstractMgrTPage.class, "/orgmgrt.css");

		addComponentBean(pp, "AbstractMgrTPage_orgSelect", DeptSelectBean.class)
				.setOrg(true)
				.setClearAction("false")
				.setJsSelectCallback(
						"location.href = location.href.addParameter('orgId=' + selects[0].id); return false;");
	}

	@Override
	public String getRole(final PageParameter pp) {
		return IOrganizationContext.ROLE_ORGANIZATION_MANAGER;
	}

	protected static Department getOrg(final PageParameter pp) {
		final IDepartmentService dService = orgContext.getDepartmentService();
		Department org = null;
		if (pp.getLogin().isManager()) {
			org = dService.getBean(pp.getParameter("orgId"));
		}
		if (org == null) {
			org = dService.getBean(pp.getLogin().getDept().getDomainId());
		}
		return org;
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		SpanElement oele;
		final Department org = getOrg(pp);
		if (org != null) {
			String txt = org.getText();
			final int nums = orgContext.getAccountStatService().getOrgCount(org, "nums");
			if (nums > 0) {
				txt += " (" + nums + ")";
			}
			oele = new SpanElement(txt);
		} else {
			oele = new SpanElement("当前用户没有机构信息");
		}
		final ElementList el = ElementList.of(oele.setClassName("org_txt"));
		if (pp.getLogin().isManager()) {
			el.append(SpanElement.SPACE).append(
					new LinkElement("选择机构").setOnclick("$Actions['AbstractMgrTPage_orgSelect']();"));
		}
		return el;
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		final OrganizationUrlsFactory urlsFactory = ((IOrganizationWebContext) orgContext)
				.getUrlsFactory();
		String params = null;
		final String orgid = pp.getParameter("orgId");
		if (StringUtils.hasText(orgid)) {
			params = "orgId=" + orgid;
		}
		return TabButtons.of(new TabButton($m("AbstractMgrTPage.0")).setHref(urlsFactory.getUrl(pp,
				DepartmentMgrTPage.class, params)), new TabButton($m("AbstractMgrTPage.1"))
				.setHref(urlsFactory.getUrl(pp, UserMgrTPage.class, params)), new TabButton(
				$m("AbstractMgrTPage.2")).setHref(urlsFactory.getUrl(pp, RoleMgrTPage.class, params)));
	}
}
