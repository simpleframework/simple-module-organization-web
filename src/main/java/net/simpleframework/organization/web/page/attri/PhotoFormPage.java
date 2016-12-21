package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import net.simpleframework.common.JsonUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.settings.ContextSettings;
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
import net.simpleframework.mvc.template.ITemplateHandler;
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

		pp.addImportCSS(ITemplateHandler.class, "/cropper.css");
		pp.addImportJavascript(ITemplateHandler.class, "/js/cropper.js");

		pp.addImportJavascript(PhotoFormPage.class, "/js/account-photo.js");

		addAjaxRequest(pp, "PhotoFormPage_gurl").setHandlerMethod("doGurl");

		addAjaxRequest(pp, "PhotoFormPage_save").setHandlerMethod("doSave");

		addComponentBean(pp, "PhotoFormPage_upload", PluploadBean.class).setMultiFileSelected(false)
				.setUploadText($m("PhotoFormPage.0")).setFileTypes(SwfUploadBean.IMAGES_FILETYPES)
				.setFileSizeLimit("2MB").setFileQueueLimit(1)
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
		final String dstr = cp.getParameter("data");
		if (!StringUtils.hasText(dstr)) {
			return JavascriptForward.RELOC;
		}

		final Map<String, ?> data = JsonUtils.toMap(dstr);
		final AttachmentFile af = (AttachmentFile) cp.getSessionAttr(PHOTO_CACHE);
		final ID loginId = cp.getLoginId();
		final User user = _accountService.getUser(loginId);

		FileInputStream istream = null;
		if (af != null) {
			istream = new FileInputStream(af.getAttachment());
		} else {
			String url = cp.getPhotoUrl(loginId, 0, 0);
			final int p = url.lastIndexOf("?");
			if (p > 0) {
				url = url.substring(0, p);
			}
			final ContextSettings settings = getModuleContext().getApplicationContext()
					.getContextSettings();
			istream = new FileInputStream(new File(
					settings.getHomeFileDir().getAbsolutePath() + url.replace("/", File.separator)));
		}

		if (istream != null) {
			try {
				final BufferedImage sbi = ImageIO.read(istream);
				int width = Convert.toInt(data.get("width"));
				int height = Convert.toInt(data.get("height"));
				final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				final Graphics2D g = bi.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, width, height);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);

				final int srcX = Convert.toInt(Convert.toInt(data.get("x")));
				final int srcY = Convert.toInt(Convert.toInt(data.get("y")));
				width = Math.min(width, sbi.getWidth());
				height = Math.min(height, sbi.getHeight());
				g.drawImage(sbi, 0, 0, width, height, srcX, srcY, srcX + width, srcY + height, null);
				g.dispose();

				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(bi, "png", os);
				_userService.updatePhoto(user, new ByteArrayInputStream(os.toByteArray()));
			} finally {
				istream.close();
				cp.removeSessionAttr(PHOTO_CACHE);
				cp.clearPhotoCache(loginId);
			}
		}
		return JavascriptForward.RELOC;
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
