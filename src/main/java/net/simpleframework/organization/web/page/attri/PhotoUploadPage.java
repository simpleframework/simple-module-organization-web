package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.simpleframework.common.AlgorithmUtils;
import net.simpleframework.common.Convert;
import net.simpleframework.common.FileUtils;
import net.simpleframework.common.ID;
import net.simpleframework.common.ImageUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.AbstractUrlForward;
import net.simpleframework.mvc.IMultipartFile;
import net.simpleframework.mvc.MultipartPageRequest;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.UrlForward;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.submit.SubmitBean;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.OrganizationException;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.bean.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class PhotoUploadPage extends AbstractAccountPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addComponentBean(pp, "uploadPhoto", SubmitBean.class).setFormName("uploadPhoto")
				.setBinary(true).setConfirmMessage($m("Confirm.Post")).setHandlerMethod("upload")
				.setHandlerClass(PhotoUploadPage.class);
		addValidationBean(pp, "uploadValidation").setTriggerSelector("#btnUploadPhoto").addValidators(
				new Validator(EValidatorMethod.file, "#user_photo").setArgs("jpg,jpeg,bmp,gif,png"));
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return PermissionConst.ROLE_ALL_ACCOUNT;
	}

	@Override
	public KVMap createVariables(final PageParameter pp) {
		final KVMap kv = super.createVariables(pp);
		final String src = pp.getParameter("src");
		if (StringUtils.hasText(src)) {
			kv.add("src", pp.wrapContextPath(new String(AlgorithmUtils.base64Decode(src))));
		}
		final String size = pp.getParameter("size");
		if (StringUtils.hasText(size)) {
			kv.add("size", FileUtils.toFileSize(Convert.toLong(size)));
		}
		return kv;
	}

	@Transaction(context = IOrganizationContext.class)
	public AbstractUrlForward upload(final ComponentParameter cp) {
		final Account account = getAccount(cp);
		final ID accountId = account.getId();
		final MultipartPageRequest request = (MultipartPageRequest) cp.request;
		final IMultipartFile multipart = request.getFile("user_photo");
		long size;
		if ((size = multipart.getSize()) > 1024 * 1024) {
			return new UrlForward(url(PhotoUploadPage.class, "size=" + size));
		} else {
			try {
				final User user = _accountService.getUser(accountId);
				final InputStream inputStream = multipart.getInputStream();
				if (inputStream != null) {
					final ByteArrayOutputStream os = new ByteArrayOutputStream();
					final double l = Math.min(102400d / Math.abs(size - 102400), 1d);
					ImageUtils.thumbnail(inputStream, l, os);
					_userService.updatePhoto(user, new ByteArrayInputStream(os.toByteArray()));
				} else {
					_userService.updatePhoto(user, null);
				}
				// 删除图片缓存
				cp.clearPhotoCache(accountId);
				return new UrlForward(url(PhotoUploadPage.class,
						"src=" + AlgorithmUtils.base64Encode(cp.getPhotoUrl(accountId).getBytes())));
			} catch (final IOException e) {
				throw OrganizationException.of(e);
			}
		}
	}
}
