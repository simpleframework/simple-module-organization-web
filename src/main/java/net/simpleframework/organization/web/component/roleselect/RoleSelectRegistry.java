package net.simpleframework.organization.web.component.roleselect;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentBean;
import net.simpleframework.mvc.component.ComponentName;
import net.simpleframework.mvc.component.ComponentResourceProvider;
import net.simpleframework.mvc.component.ComponentUtils;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryRegistry;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@ComponentName(RoleSelectRegistry.ROLESELECT)
@ComponentBean(RoleSelectBean.class)
@ComponentResourceProvider(RoleSelectResourceProvider.class)
public class RoleSelectRegistry extends DictionaryRegistry {
	public static final String ROLESELECT = "roleSelect";

	@Override
	public RoleSelectBean createComponentBean(final PageParameter pp, final Object attriData) {
		final RoleSelectBean roleSelect = (RoleSelectBean) super.createComponentBean(pp, attriData);

		final AjaxRequestBean ajaxRequest = (AjaxRequestBean) roleSelect.getAttr(ATTRI_AJAXREQUEST);
		if (ajaxRequest != null) {
			ajaxRequest.setUrlForward(ComponentUtils.getResourceHomePath(RoleSelectBean.class)
					+ "/jsp/role_select.jsp?" + RoleSelectUtils.BEAN_ID + "=" + roleSelect.hashId());
		}
		return roleSelect;
	}
}
