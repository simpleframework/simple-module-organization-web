package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;

import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.bean.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class PhotoPage extends AbstractAccountPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addAjaxRequest(pp, "PhotoPage_del").setHandlerMethod("doDel").setConfirmMessage(
				$m("PhotoPage.1"));
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDel(final ComponentParameter cp) throws IOException {
		final User user = _accountService.getUser(cp.getLogin());
		_userService.updatePhoto(user, null);
		// 删除图片缓存
		cp.clearPhotoCache(user);
		return new JavascriptForward("$('user_edit_photo_image').src = '").append(
				cp.getPhotoUrl(user.getId())).append("';");
	}

	public String getPhotoUrl(final PageParameter pp, final Account account) {
		return pp.getPhotoUrl(account.getId());
	}

	public String getUploadUrl(final PageParameter pp, final Account account) {
		return pp.getContextPath() + url(PhotoUploadPage.class, "accountId=" + account.getId());
	}
}
