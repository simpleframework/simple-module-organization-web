package net.simpleframework.organization.web.page.attri;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ui.window.WindowRegistry;
import net.simpleframework.mvc.template.lets.Category_BlankPage;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.CategoryItems;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.web.IOrganizationWebContext;
import net.simpleframework.organization.web.OrganizationUrlsFactory;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractAttriTPage extends Category_BlankPage implements
		IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(AbstractAttriTPage.class, "/account_attri.css");
	}

	@Override
	public String[] getDependentComponents(final PageParameter pp) {
		return new String[] { WindowRegistry.WINDOW };
	}

	static final OrganizationUrlsFactory uFactory = ((IOrganizationWebContext) orgContext)
			.getUrlsFactory();

	@SuppressWarnings("unchecked")
	@Override
	protected CategoryItems getCategoryList(final PageParameter pp) {
		final CategoryItems blocks = CategoryItems.of();
		int i = 0;
		final String[] icons = new String[] { "img_user", "img_account", "img_password", "img_photo" };
		for (final Class<? extends AbstractMVCPage> pageClass : new Class[] { UserAttriTPage.class,
				AccountStatTPage.class, AccountPasswordTPage.class, PhotoTPage.class }) {
			blocks.add(new CategoryItem($m("AbstractEditAwarePage." + i))
					.setHref(uFactory.getUrl(pp, pageClass)).setIconClass(icons[i++])
					.setSelected(pageClass.isAssignableFrom(getOriginalClass())));
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
