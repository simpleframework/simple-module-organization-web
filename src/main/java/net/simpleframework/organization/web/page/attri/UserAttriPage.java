package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ctx.permission.IPermissionConst;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.organization.web.page.AbstractAccountAttriPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserAttriPage extends AbstractAccountAttriPage {

	@Override
	public String getRole(final PageParameter pp) {
		return IPermissionConst.ROLE_ALL_ACCOUNT;
	}

	@Override
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		super.onSave(cp);
		return new JavascriptForward("alert('").append($m("UserAttriPage.0")).append("');");
	}

	@Override
	public void onLoad(final PageParameter pp, final Map<String, Object> dataBinding,
			final PageSelector selector) {
		super.onLoad(pp, dataBinding, selector);
		selector.readonlySelector = "#ae_accountName";
	}

	@Override
	public boolean isButtonsOnTop(final PageParameter pp) {
		return true;
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(SpanElement.strongText($m("UserAttriPage.1")));
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		return ElementList.of(SAVE_BTN());
	}

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		return TableRows.of(r1, r3, r4, r5, r6, r7, r8, r9, r10);
	}
}
