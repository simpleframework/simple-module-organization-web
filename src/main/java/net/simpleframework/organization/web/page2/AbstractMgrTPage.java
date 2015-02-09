package net.simpleframework.organization.web.page2;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.ID;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.template.lets.Tabs_BlankPage;
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
public abstract class AbstractMgrTPage extends Tabs_BlankPage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		pp.addImportCSS(AbstractMgrTPage.class, "/orgmgrt.css");
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final ID deptId = pp.getLogin().getDept().getId();
		if (deptId != null) {
			final Department org = orgContext.getDepartmentService().getOrg(deptId);
			String txt = org.getText();
			final int nums = orgContext.getAccountStatService().getOrgCount(org, "nums");
			if (nums > 0) {
				txt += " (" + nums + ")";
			}
			return ElementList.of(new SpanElement(txt).setClassName("org_txt"));
		}
		return super.getLeftElements(pp);
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		final OrganizationUrlsFactory urlsFactory = ((IOrganizationWebContext) orgContext)
				.getUrlsFactory();
		return TabButtons.of(new TabButton($m("AbstractMgrTPage.0")).setHref(urlsFactory.getUrl(pp,
				DepartmentMgrTPage.class)), new TabButton($m("AbstractMgrTPage.1")).setHref(urlsFactory
				.getUrl(pp, UserMgrTPage.class)), new TabButton($m("AbstractMgrTPage.2"))
				.setHref(urlsFactory.getUrl(pp, RoleMgrTPage.class)));
	}
}
