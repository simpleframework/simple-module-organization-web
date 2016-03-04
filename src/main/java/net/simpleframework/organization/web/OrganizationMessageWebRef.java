package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.IContextBase;
import net.simpleframework.module.msg.plugin.NoticeMessageCategory;
import net.simpleframework.module.msg.plugin.NoticeMessagePlugin;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.OrganizationMessageRef;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class OrganizationMessageWebRef extends OrganizationMessageRef {

	protected NoticeMessageCategory MC_PASSWORD_EDIT, MC_PASSWORD_GET;

	@Override
	public void onInit(final IContextBase context) throws Exception {
		super.onInit(context);

		final NoticeMessagePlugin plugin = getNoticeMessagePlugin();

		MC_PASSWORD_EDIT = new NoticeMessageCategory("MC_PASSWORD_EDIT",
				$m("OrganizationMessageWebRef.0"), $m("OrganizationMessageWebRef.1"),
				ClassUtils.getResourceAsString(OrganizationMessageWebRef.class, "MC_PASSWORD_EDIT.txt"));
		plugin.registMessageCategory(setGroup(MC_PASSWORD_EDIT));

		MC_PASSWORD_GET = new NoticeMessageCategory("MC_PASSWORD_GET",
				$m("OrganizationMessageWebRef.2"), $m("OrganizationMessageWebRef.3"),
				ClassUtils.getResourceAsString(OrganizationMessageWebRef.class, "MC_PASSWORD_GET.txt"))
				.setSendTo_normal(false).setSendTo_email(true);
		plugin.registMessageCategory(setGroup(MC_PASSWORD_GET));
	}

	public void doPasswordEditMessage(final Account account, final String password) {
		if (MC_PASSWORD_EDIT == null) {
			return;
		}
		getNoticeMessagePlugin().sentMessage(account.getId(), MC_PASSWORD_EDIT,
				new KVMap().add("account", account).add("password", password));
	}

	public void doPasswordGetMessage(final Account account, final String code) {
		if (MC_PASSWORD_GET == null) {
			return;
		}
		getNoticeMessagePlugin().sentMessage(account.getId(), MC_PASSWORD_GET,
				new KVMap().add("code", code));
	}
}
