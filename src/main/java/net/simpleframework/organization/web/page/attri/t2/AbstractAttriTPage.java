package net.simpleframework.organization.web.page.attri.t2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectUtils;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ui.window.WindowRegistry;
import net.simpleframework.mvc.template.lets.Category_BlankPage;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.CategoryItems;
import net.simpleframework.organization.web.page.attri.AbstractAccountPage;
import net.simpleframework.organization.web.page.attri.AccountPasswordPage;
import net.simpleframework.organization.web.page.attri.AccountStatPage;
import net.simpleframework.organization.web.page.attri.PhotoPage;
import net.simpleframework.organization.web.page.attri.UserAttriPage;
import net.simpleframework.organization.web.page.attri.t2.AbstractAttriPage.AccountPasswordPageT2;
import net.simpleframework.organization.web.page.attri.t2.AbstractAttriPage.AccountStatPageT2;
import net.simpleframework.organization.web.page.attri.t2.AbstractAttriPage.PhotoPageT2;
import net.simpleframework.organization.web.page.attri.t2.AbstractAttriPage.UserAttriPageT2;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractAttriTPage extends Category_BlankPage {

	@Override
	protected void addImportCSS(final PageParameter pp) {
		super.addImportCSS(pp);

		pp.addImportCSS(AbstractAccountPage.class, "/account_attri.css");
	}

	@Override
	public String[] getDependentComponents(final PageParameter pp) {
		return new String[] { WindowRegistry.WINDOW };
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CategoryItems getCategoryList(final PageParameter pp) {
		final CategoryItems blocks = CategoryItems.of();
		int i = 0;
		final String[] icons = new String[] { "img_user", "img_account", "img_password", "img_photo" };
		for (final Class<? extends AbstractMVCPage> pageClass : new Class[] { UserAttriPageT2.class,
				AccountStatPageT2.class, AccountPasswordPageT2.class, PhotoPageT2.class }) {
			final String hash = ObjectUtils.hashStr(pageClass.getSimpleName());
			final CategoryItem block = new CategoryItem($m("AbstractEditAwarePage." + i)).setHref(
					url(pageClass, "h=" + hash)).setIconClass(icons[i++]);
			final String h = pp.getParameter("h");
			block.setSelected(StringUtils.hasText(h) ? h.equals(hash) : i == 1);
			blocks.add(block);
		}
		return blocks;
	}

	public static class UserAttriTPage extends AbstractAttriTPage {

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			final StringBuilder sb = new StringBuilder();
			sb.append("<div class='UserAttriTPage'>");
			sb.append(pp.includeUrl(UserAttriPage.class));
			sb.append("</div>");
			return sb.toString();
		}
	}

	public static class AccountStatTPage extends AbstractAttriTPage {
		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			final StringBuilder sb = new StringBuilder();
			sb.append("<div class='AccountStatTPage'>");
			sb.append(pp.includeUrl(AccountStatPage.class));
			sb.append("</div>");
			return sb.toString();
		}
	}

	public static class AccountPasswordTPage extends AbstractAttriTPage {
		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			final StringBuilder sb = new StringBuilder();
			sb.append("<div class='AccountPasswordTPage'>");
			sb.append(pp.includeUrl(AccountPasswordPage.class));
			sb.append("</div>");
			return sb.toString();
		}
	}

	public static class PhotoTPage extends AbstractAttriTPage {
		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			final StringBuilder sb = new StringBuilder();
			sb.append("<div class='PhotoTPage'>");
			sb.append(pp.includeUrl(PhotoPage.class));
			sb.append("</div>");
			return sb.toString();
		}
	}
}
