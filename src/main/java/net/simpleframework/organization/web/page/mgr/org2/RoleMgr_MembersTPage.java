package net.simpleframework.organization.web.page.mgr.org2;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.mvc.PageParameter;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RoleMgr_MembersTPage extends AbstractOrgMgrTPage {

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='RoleMgrTPage clearfix'>");
		sb.append(" <div class='lnav'>");
		sb.append("  <div class='lbl'>#(RoleMgrTPage.5)</div>");
		sb.append(" </div>");
		sb.append(" <div class='rtbl'>");
		sb.append("  <div class='tbar'>");
		sb.append("  </div>");
		sb.append("  <div id='idRoleMgrTPage_tbl'></div>");
		sb.append(" </div>");
		sb.append("</div>");
		return sb.toString();
	}
}
