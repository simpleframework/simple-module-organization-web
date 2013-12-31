package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.organization.Account;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class PhotoPage extends AbstractAccountPage {

	public String getPhotoUrl(final PageParameter pp, final Account account) {
		return pp.getPhotoUrl(account.getId(), 164, 164);
	}

	public String getUploadUrl(final PageParameter pp, final Account account) {
		return pp.getContextPath() + url(PhotoUploadPage.class, "accountId=" + account.getId());
	}

	@Override
	public KVMap createVariables(final PageParameter pp) {
		return super.createVariables(pp).add("nav",
				BlockElement.nav().addElements(SpanElement.strongText($m("PhotoPage.0"))));
	}
}
