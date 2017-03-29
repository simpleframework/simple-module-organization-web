package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.ImageUtils;
import net.simpleframework.common.JsonUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.IMultipartFile;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.ImageCache;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.plupload.PluploadBean;
import net.simpleframework.mvc.component.ui.swfupload.AbstractSwfUploadHandler;
import net.simpleframework.mvc.component.ui.swfupload.SwfUploadBean;
import net.simpleframework.mvc.impl.DefaultPageResourceProvider;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.bean.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class PhotoFormPage extends AbstractAccountFormPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(DefaultPageResourceProvider.class, "/cropper.css");
		pp.addImportJavascript(DefaultPageResourceProvider.class, "/js/cropper.js");

		pp.addImportJavascript(PhotoFormPage.class, "/js/account-photo.js");

		addAjaxRequest(pp, "PhotoFormPage_gurl").setHandlerMethod("doGurl");

		addAjaxRequest(pp, "PhotoFormPage_save").setHandlerMethod("doSave");

		addComponentBean(pp, "PhotoFormPage_upload", PluploadBean.class).setMultiFileSelected(false)
				.setUploadText($m("PhotoFormPage.0")).setFileTypes(SwfUploadBean.IMAGES_FILETYPES)
				.setFileSizeLimit("5MB").setFileQueueLimit(1)
				.setJsCompleteCallback("$Actions['PhotoFormPage_gurl']();")
				.setContainerId("idPhotoFormPage_upload").setHandlerClass(PhotoUploadHandler.class);
	}

	public IForward doGurl(final ComponentParameter cp) throws IOException {
		final AttachmentFile af = (AttachmentFile) cp.getSessionAttr(PHOTO_CACHE);
		if (af != null) {
			return new JavascriptForward("PhotoTPage.photo_set('")
					.append(new ImageCache().getPath(cp, af)).append("');");
		}
		return null;
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doSave(final ComponentParameter cp) throws Exception {
		final AttachmentFile af = (AttachmentFile) cp.getSessionAttr(PHOTO_CACHE);
		final ID loginId = cp.getLoginId();
		final User user = _accountService.getUser(loginId);

		FileInputStream istream = null;
		try {
			final String dstr = cp.getParameter("data");
			if (!StringUtils.hasText(dstr)) {
				if (af != null) {
					_userService.updatePhoto(user, (istream = new FileInputStream(af.getAttachment())));
				}
				return JavascriptForward.RELOC;
			}

			final Map<String, ?> data = JsonUtils.toMap(dstr);
			if (af != null) {
				istream = new FileInputStream(af.getAttachment());
			} else {
				cp.getPhotoUrl(loginId, 0, 0);
				final File photoFile = (File) cp.getRequestAttr("_photoFile");
				if (photoFile != null) {
					istream = new FileInputStream(photoFile);
				}
			}

			if (istream != null) {
				final int width = Convert.toInt(data.get("width"));
				final int height = Convert.toInt(data.get("height"));
				final int srcX = Convert.toInt(data.get("x"));
				final int srcY = Convert.toInt(data.get("y"));
				final BufferedImage bi = ImageUtils.clip(istream, width, height, srcX, srcY);

				final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
				final int w = bi.getWidth();
				if (w > 480) {
					ImageUtils.thumbnail(bi, 480.0 / w, oStream, "png");
				} else {
					ImageIO.write(bi, "png", oStream);
				}
				_userService.updatePhoto(user, new ByteArrayInputStream(oStream.toByteArray()));
			}
			return JavascriptForward.RELOC;
		} finally {
			if (istream != null) {
				istream.close();
			}
			cp.removeSessionAttr(PHOTO_CACHE);
			cp.clearPhotoCache(loginId);
		}
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='PhotoFormPage'>");
		sb.append(" <div id='idPhotoFormPage_upload'></div>");
		sb.append(" <div class='img-c'>");
		sb.append("  <div class='cc'>");
		sb.append(
				new ImageElement(pp.getPhotoUrl(pp.getLoginId(), 0, 0)).setId("idPhotoFormPage_img"));
		sb.append("  </div>");
		sb.append("  <div class='bb'>");
		sb.append(LinkButton.corner($m("PhotoFormPage.1")).setOnclick("PhotoTPage.photo_clip();"));
		sb.append(SpanElement.SPACE10);
		sb.append(LinkButton.corner($m("PhotoFormPage.2")).setHighlight(true)
				.setOnclick("PhotoTPage.photo_save();"));
		sb.append("  </div>");
		sb.append(" </div>");
		sb.append(" <div class='desc'>").append($m("PhotoFormPage.3")).append("</div>");
		sb.append("</div>");
		return sb.toString();
	}

	private static String PHOTO_CACHE = "PHOTO_CACHE";

	public static class PhotoUploadHandler extends AbstractSwfUploadHandler {
		@Override
		public void upload(final ComponentParameter cp, final IMultipartFile multipartFile,
				final Map<String, Object> variables) throws Exception {
			cp.setSessionAttr(PHOTO_CACHE, new AttachmentFile(multipartFile.getFile()));
		}
	}
}
