package net.simpleframework.organization.web.page.attri;

import net.simpleframework.mvc.PageParameter;
import net.simpleframework.organization.bean.Account;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class PhotoPage extends AbstractAccountPage {

	public String getPhotoUrl(final PageParameter pp, final Account account) {
		return pp.getPhotoUrl(account.getId());
	}

	public String getUploadUrl(final PageParameter pp, final Account account) {
		return pp.getContextPath() + url(PhotoUploadPage.class, "accountId=" + account.getId());
	}
}
