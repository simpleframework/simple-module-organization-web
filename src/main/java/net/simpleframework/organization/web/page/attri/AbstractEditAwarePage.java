package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.mvc.MVCUtils;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.component.ui.tabs.TabItem;
import net.simpleframework.mvc.component.ui.tabs.TabsBean;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractEditAwarePage extends AbstractTemplatePage
		implements IOrganizationContextAware {

	protected void addComponent_editUserWin(final PageParameter pp) {
		addAjaxRequest(pp, "taAttri_1", UserAttriPage.class);
		addAjaxRequest(pp, "taAttri_2", AccountStatPage.class);
		addAjaxRequest(pp, "taAttri_3", AccountPasswordPage.class);
		addAjaxRequest(pp, "taAttri_4", PhotoPage.class);

		addComponentBean(pp, "taAttri", TabsBean.class)
				.addTab(new TabItem($m("AbstractEditAwarePage.0")).setContentRef("taAttri_1")
						.setCache(true))
				.addTab(new TabItem($m("AbstractEditAwarePage.1")).setContentRef("taAttri_2")
						.setCache(true))
				.addTab(new TabItem($m("AbstractEditAwarePage.2")).setContentRef("taAttri_3")
						.setCache(true))
				.addTab(new TabItem($m("AbstractEditAwarePage.3")).setContentRef("taAttri_4")
						.setCache(true));

		addWindowBean(pp, "AbstractEditAwarePage_editUserWin").setContentRef("taAttri")
				.setTitle($m("AbstractEditAwarePage.4")).setHeight(480).setWidth(640);
	}

	public AbstractElement<?> str_Login(final PageParameter pp) {
		return new LinkElement($m("AbstractEditAwarePage.5"))
				.setOnclick(JS.loc(MVCUtils.getLocationPath()));
	}

	public AbstractElement<?> str_Logout(final PageParameter pp) {
		return new LinkElement($m("AbstractEditAwarePage.6"))
				.setOnclick("$Actions['AbstractTemplatePage_logout']();");
	}

	public AbstractElement<?> str_Photo(final PageParameter pp) {
		return new ImageElement(pp.getPhotoUrl()).setClassName("photo_icon");
	}
}
