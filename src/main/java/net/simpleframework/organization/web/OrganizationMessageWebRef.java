package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;

import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.IContextBase;
import net.simpleframework.module.msg.plugin.NoticeMessageCategory;
import net.simpleframework.module.msg.plugin.NoticeMessagePlugin;
import net.simpleframework.organization.OrganizationMessageRef;
import net.simpleframework.organization.bean.Account;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class OrganizationMessageWebRef extends OrganizationMessageRef {
	protected String PASSWORD_EDIT = $m("OrganizationMessageWebRef.0");
	protected String PASSWORD_RESET = $m("OrganizationMessageWebRef.1");

	protected NoticeMessageCategory MC_PASSWORD_EDIT, MC_PASSWORD_RESET_EMAIL,
			MC_PASSWORD_RESET_MOBILE;

	@Override
	public void onInit(final IContextBase context) throws Exception {
		super.onInit(context);

		final NoticeMessagePlugin plugin = getNoticeMessagePlugin();
		plugin.registMessageCategory(setGroup(MC_PASSWORD_EDIT = MC_PASSWORD_EDIT()));
		plugin.registMessageCategory(setGroup(MC_PASSWORD_RESET_EMAIL = MC_PASSWORD_RESET_EMAIL()));
		plugin.registMessageCategory(setGroup(MC_PASSWORD_RESET_MOBILE = MC_PASSWORD_RESET_MOBILE()));

	}

	protected NoticeMessageCategory MC_PASSWORD_EDIT() throws IOException {
		return new NoticeMessageCategory("MC_PASSWORD_EDIT", PASSWORD_EDIT, PASSWORD_EDIT, ClassUtils
				.getResourceAsString(OrganizationMessageWebRef.class, "MC_PASSWORD_EDIT.txt"));
	}

	protected NoticeMessageCategory MC_PASSWORD_RESET_EMAIL() throws IOException {
		final String cc = ClassUtils.getResourceAsString(OrganizationMessageWebRef.class,
				"MC_PASSWORD_RESET.txt");
		return new NoticeMessageCategory("MC_PASSWORD_RESET_EMAIL", PASSWORD_RESET, PASSWORD_RESET,
				cc).setSendTo_normal(false).setSendTo_email(true);
	}

	protected NoticeMessageCategory MC_PASSWORD_RESET_MOBILE() throws IOException {
		final String cc = ClassUtils.getResourceAsString(OrganizationMessageWebRef.class,
				"MC_PASSWORD_RESET.txt");
		return new NoticeMessageCategory("MC_PASSWORD_RESET_MOBILE", PASSWORD_RESET, null, cc)
				.setSendTo_normal(false).setSendTo_mobile(true);
	}

	public void doPasswordEditMessage(final Account account, final String password) {
		if (MC_PASSWORD_EDIT == null) {
			return;
		}
		getNoticeMessagePlugin().sentMessage(account.getId(), MC_PASSWORD_EDIT,
				new KVMap().add("account", account).add("password", password));
	}

	public void doPasswordResetEmailMessage(final Account account, final String code) {
		if (MC_PASSWORD_RESET_EMAIL == null) {
			return;
		}
		getNoticeMessagePlugin().sentMessage(account.getId(), MC_PASSWORD_RESET_EMAIL,
				new KVMap().add("code", code));
	}

	public void doPasswordResetMobileMessage(final Account account, final String code) {
		if (MC_PASSWORD_RESET_MOBILE == null) {
			return;
		}
		getNoticeMessagePlugin().sentMessage(account.getId(), MC_PASSWORD_RESET_MOBILE,
				new KVMap().add("code", code));
	}
}
