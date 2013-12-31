package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import net.simpleframework.common.AlgorithmUtils;
import net.simpleframework.common.ID;
import net.simpleframework.common.ImageUtils;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.AbstractUrlForward;
import net.simpleframework.mvc.IMultipartFile;
import net.simpleframework.mvc.MVCUtils;
import net.simpleframework.mvc.MultipartPageRequest;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.UrlForward;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.submit.SubmitBean;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.IUserService;
import net.simpleframework.organization.OrganizationException;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class PhotoUploadPage extends AbstractAccountPage {

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		addComponentBean(pp, "uploadPhoto", SubmitBean.class).setFormName("uploadPhoto")
				.setBinary(true).setConfirmMessage($m("Confirm.Post")).setHandleMethod("upload")
				.setHandleClass(PhotoUploadPage.class);
		addValidationBean(pp, "uploadValidation").setTriggerSelector("#btnUploadPhoto")
				.addValidators(
						new Validator(EValidatorMethod.file, "#user_photo")
								.setArgs("jpg,jpeg,bmp,gif,png"));
	}

	@Transaction(context = IOrganizationContext.class)
	public AbstractUrlForward upload(final ComponentParameter cp) {
		final Account account = getAccount(cp);
		final ID accountId = account.getId();
		final MultipartPageRequest request = (MultipartPageRequest) cp.request;
		final IMultipartFile multipart = request.getFile("user_photo");
		long size;
		if ((size = multipart.getSize()) > 1024 * 1024) {
			return new UrlForward(AbstractMVCPage.url(PhotoUploadResultPage.class, "accountId="
					+ accountId + "&error=size"));
		} else {
			try {
				final IUserService uService = context.getUserService();
				final InputStream inputStream = multipart.getInputStream();
				if (inputStream != null) {
					final ByteArrayOutputStream os = new ByteArrayOutputStream();
					final double l = Math.min(102400d / Math.abs(size - 102400), 1d);
					ImageUtils.thumbnail(inputStream, l, os);
					uService.updatePhoto(accountId, new ByteArrayInputStream(os.toByteArray()));
				} else {
					uService.updatePhoto(accountId, null);
				}
				deletePhoto(cp, accountId);
				return new UrlForward(url(
						PhotoUploadResultPage.class,
						"accountId="
								+ accountId
								+ "&src="
								+ AlgorithmUtils.base64Encode((get(PhotoPage.class)
										.getPhotoUrl(cp, account) + "?c=" + System.currentTimeMillis())
										.getBytes())));
			} catch (final IOException e) {
				throw OrganizationException.of(e);
			}
		}
	}

	private void deletePhoto(final PageRequestResponse requestResponse, final Object userId) {
		final File photoCache = new File(MVCUtils.getRealPath(MVCUtils.getPageResourcePath()
				+ "/images/photo-cache/"));
		if (!photoCache.exists()) {
			return;
		}
		for (final File photo : photoCache.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.startsWith(userId + "_");
			}
		})) {
			photo.delete();
		}
	}
}
