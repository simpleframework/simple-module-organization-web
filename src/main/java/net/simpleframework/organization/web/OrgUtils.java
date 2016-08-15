package net.simpleframework.organization.web;

import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class OrgUtils implements IOrganizationContextAware {

	public static String getUrl(final PageParameter pp, final Class<? extends AbstractMVCPage> mClass) {
		return uFactory.getUrl(pp, mClass);
	}

	public static String getUrl(final PageParameter pp,
			final Class<? extends AbstractMVCPage> mClass, final String params) {
		return uFactory.getUrl(pp, mClass, params);
	}

	static final OrganizationUrlsFactory uFactory = ((IOrganizationWebContext) orgContext)
			.getUrlsFactory();
}
