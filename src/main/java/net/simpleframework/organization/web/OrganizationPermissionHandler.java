package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.coll.CollectionUtils.NestEnumeration;
import net.simpleframework.ctx.permission.PermissionRole;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.ctx.permission.DefaultPagePermissionHandler;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EAccountStatus;
import net.simpleframework.organization.EAccountType;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IAccountService;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.IUserService;
import net.simpleframework.organization.LoginObject;
import net.simpleframework.organization.OrganizationException;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.User;
import net.simpleframework.organization.web.page.LoginWindowRedirect;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class OrganizationPermissionHandler extends DefaultPagePermissionHandler implements
		IOrganizationContextAware {

	protected User getUserObject(Object o) {
		if (o instanceof User) {
			return (User) o;
		}
		final IUserService uService = context.getUserService();
		if (o instanceof String) {
			final String s = (String) o;
			if (s.contains("@")) {
				return uService.getUserByMail(s);
			} else {
				final Account account = context.getAccountService().getAccountByName(s);
				if (account != null) {
					o = account;
				}
			}
		}
		if (o instanceof Account) {
			o = ((Account) o).getId();
		}
		return uService.getBean(o);
	}

	@Override
	public PermissionUser getUser(final Object user) {
		final User oUser = getUserObject(user);
		if (oUser == null) {
			return super.getUser(user);
		}
		return new PermissionUser() {
			@Override
			public ID getId() {
				return oUser.getId();
			}

			@Override
			public String getName() {
				if (user instanceof String) {
					return (String) user;
				}

				final Account account = context.getAccountService().getBean(user);
				return account != null ? account.getName() : null;
			}

			@Override
			public String getText() {
				return oUser.getText();
			}

			@Override
			public String getEmail() {
				return oUser.getEmail();
			}

			@Override
			public ID getOrgId() {
				final IDepartmentService service = context.getDepartmentService();
				Department org = service.getBean(oUser.getDepartmentId());
				if (org == null) {
					return null;
				}
				while (org.getDepartmentType() != EDepartmentType.organization) {
					final Department org2 = service.getBean(org.getParentId());
					if (org2 != null) {
						org = org2;
					} else {
						break;
					}
				}
				return org.getId();
			}

			@Override
			public String toOrgText() {
				final Department org = context.getDepartmentService().getBean(getOrgId());
				return org != null ? org.toString() : null;
			}

			@Override
			public InputStream getPhotoStream() {
				return context.getUserService().getPhoto(oUser);
			}

			@Override
			public ID getRoleId() {
				return context.getRoleService().getPrimaryRole(oUser).getId();
			}

			private final IRoleService rService = context.getRoleService();

			@Override
			public boolean isMember(final Object role, final Map<String, Object> variables) {
				return rService.isMember(oUser, getRoleObject(role), variables);
			}

			@Override
			public boolean isManager(final Map<String, Object> variables) {
				return rService.isManager(oUser, variables);
			}

			private static final long serialVersionUID = -2824016565752293671L;
		};
	}

	protected Role getRoleObject(final Object o) {
		if (o instanceof Role) {
			return (Role) o;
		}
		final IRoleService service = context.getRoleService();
		if (o instanceof String) {
			return service.getRoleByName((String) o);
		}
		return service.getBean(o);
	}

	@Override
	public PermissionRole getRole(final Object role) {
		final Role oRole = getRoleObject(role);
		if (oRole == null) {
			return super.getRole(role);
		}
		return new PermissionRole() {
			@Override
			public ID getId() {
				return oRole.getId();
			}

			@Override
			public String getName() {
				return context.getRoleService().toUniqueName(oRole);
			}

			@Override
			public String getText() {
				return oRole.getText();
			}

			private static final long serialVersionUID = 4548851646225261207L;
		};
	}

	@Override
	public Enumeration<ID> users(final Object role, final Map<String, Object> variables) {
		return new NestEnumeration<ID, User>(context.getRoleService().users(getRoleObject(role),
				variables)) {
			@Override
			protected ID change(final User n) {
				return n.getId();
			}
		};
	}

	@Override
	public Enumeration<ID> roles(final Object user, final Map<String, Object> variables) {
		return new NestEnumeration<ID, Role>(context.getRoleService().roles(getUserObject(user),
				variables, Convert.toBool(variables.get("ruleRole"), false))) {
			@Override
			protected ID change(final Role n) {
				return n.getId();
			}
		};
	}

	@Override
	public ID getLoginId(final PageRequestResponse rRequest) {
		return context.getAccountService().getLoginId(new HttpAccountSession(rRequest));
	}

	public static final String ACCOUNT_TYPE = "accountType";

	@Override
	public void login(final PageRequestResponse rRequest, final String login, final String password,
			final Map<String, Object> params) {
		final HttpAccountSession accountSession = new HttpAccountSession(rRequest);
		final IAccountService service = context.getAccountService();

		EAccountType accountType = null;
		if (params != null) {
			accountType = (EAccountType) params.get(ACCOUNT_TYPE);
		}
		if (accountType == null) {
			accountType = EAccountType.normal;
		}

		Account account = null;
		if (accountType == EAccountType.normal) {
			account = service.getAccountByName(login);
		}
		if (account == null) {
			throw OrganizationException.of($m("OrganizationPermission.1"));
		} else {
			final ID loginId = service.getLoginId(accountSession);
			if (loginId != null && loginId.equals(account.getId())) {
				throw OrganizationException.of($m("OrganizationPermission.0")).setCode(
						OrganizationException.CODE_LOGGED);
			}
			if (!service.verifyPassword(account, password)) {
				throw OrganizationException.of($m("OrganizationPermission.2")).putVal("password",
						Boolean.TRUE);
			} else {
				final EAccountStatus status = account.getStatus();
				if (status == EAccountStatus.normal) {
					service.setLogin(accountSession, new LoginObject(account.getId())
							.setDescription($m("OrganizationPermissionHandler.0")));
				} else if (status == EAccountStatus.locked) {
					throw OrganizationException.of($m("OrganizationPermission.3"));
				} else if (status == EAccountStatus.registration) {
					throw OrganizationException.of($m("OrganizationPermission.4"));
				} else if (status == EAccountStatus.delete) {
					throw OrganizationException.of($m("OrganizationPermission.5"));
				}
			}
		}
	}

	@Override
	public void logout(final PageRequestResponse rRequest) {
		context.getAccountService().logout(new HttpAccountSession(rRequest));
	}

	@Override
	public String getLoginRedirectUrl(final PageRequestResponse rRequest, final String roleName) {
		final HttpAccountSession accountSession = new HttpAccountSession(rRequest);
		final LoginObject loginObject = accountSession.getAutoLogin();
		if (loginObject != null) {
			doAutoLogin(accountSession, loginObject);
			return null;
		}
		return super.getLoginRedirectUrl(rRequest, roleName);
	}

	protected void doAutoLogin(final HttpAccountSession accountSession, final LoginObject loginObject) {
		context.getAccountService().setLogin(accountSession, loginObject);
	}

	@Override
	protected String getLoginWindowRedirectUrl(final PageRequestResponse rRequest) {
		return AbstractMVCPage.url(LoginWindowRedirect.class);
	}
}
