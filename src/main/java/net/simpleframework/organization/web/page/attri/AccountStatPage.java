package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.organization.IAccount;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class AccountStatPage extends AbstractAccountPage {

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final IAccount account = getAccount(pp);
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='AccountStatPage'>");
		sb.append("<table class='form_tbl' cellspacing='1'>");
		sb.append("  <tr>");
		sb.append("    <td class='l'>#(AccountStatPage.0)</td>");
		sb.append("    <td class='v'>").append(account.getStatus()).append("</td>");
		sb.append("  </tr>");
		sb.append("  <tr>");
		sb.append("    <td class='l'>#(AccountStatPage.1)</td>");
		sb.append("    <td class='v'>").append(d(account.getCreateDate())).append("</td>");
		sb.append("  </tr>");
		sb.append("  <tr>");
		sb.append("    <td class='l'>#(AccountStatPage.2)</td>");
		sb.append("    <td class='v'>").append(d(account.getLastLoginDate())).append("</td>");
		sb.append("  </tr>");
		sb.append("  <tr>");
		sb.append("    <td class='l'>#(AccountStatPage.3)</td>");
		sb.append("    <td class='v'>").append(StringUtils.blank(account.getLastLoginIP()))
				.append("</td>");
		sb.append("  </tr>");
		sb.append("  <tr>");
		sb.append("    <td class='l'>#(AccountStatPage.4)</td>");
		sb.append("    <td class='v'>").append(account.getLoginTimes()).append("</td>");
		sb.append("  </tr>");
		sb.append("  <tr>");
		sb.append("    <td class='l'>#(AccountStatPage.5)</td>");
		sb.append("    <td class='v'>").append(DateUtils.toDifferenceDate(account.getOnlineMillis()))
				.append("</td>");
		sb.append("  </tr>");
		sb.append("  <tr>");
		sb.append("    <td class='l'>#(AccountStatPage.6)</td>");
		sb.append("    <td class='v'>").append(b(account.isMailbinding())).append("</td>");
		sb.append("  </tr>");
		sb.append("  <tr>");
		sb.append("    <td class='l'>#(AccountStatPage.7)</td>");
		sb.append("    <td class='v'>").append(b(account.isMobilebinding())).append("</td>");
		sb.append("  </tr>");
		sb.append("</table>");
		sb.append("</div>");
		return sb.toString();
	}

	private String d(final Date d) {
		return StringUtils.blank(Convert.toDateString(d));
	}

	private String b(final boolean b) {
		return $m(b ? "AccountStatPage.9" : "AccountStatPage.10");
	}
}
