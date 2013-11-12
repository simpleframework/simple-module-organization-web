package net.simpleframework.organization.web.page.attri;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.common.AlgorithmUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.mvc.PageParameter;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class PhotoUploadResultPage extends AbstractAccountPage {

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String variable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><head></head><body><div class='PhotoUploadResultPage'>");
		final String error = pp.getParameter("error");
		if ("size".equals(error)) {
			sb.append("<div class='info'>#(PhotoUploadResultPage.0)</div>");
		} else {
			final String src = pp.getParameter("src");
			if (StringUtils.hasText(src)) {
				sb.append("<div class='info'>#(PhotoUploadResultPage.2)</div>");
				sb.append(TAG_SCRIPT_START);
				sb.append("(function() {");
				sb.append("parent.$('user_edit_photo_image').src = '")
						.append(new String(AlgorithmUtils.base64Decode(src))).append("';");
				sb.append("})();");
				sb.append(TAG_SCRIPT_END);
			}
		}
		sb.append("<p><input type='button' value='#(PhotoUploadResultPage.1)' onclick='history.back();'/></p>");
		sb.append("</div></body></html>");
		return sb.toString();
	}
}
