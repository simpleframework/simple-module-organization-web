package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import net.simpleframework.common.ID;
import net.simpleframework.common.coll.CollectionUtils.NestIterator;
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
		final IUserService uService = orgContext.getUserService();
		if (o instanceof String) {
			final String s = (String) o;
			if (s.contains("@")) {
				return uService.getUserByEmail(s);
			} else {
				final Account account = orgContext.getAccountService().getAccountByName(s);
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

				final Account account = orgContext.getAccountService().getBean(user);
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
			public String getSex() {
				return oUser.getSex();
			}

			@Override
			public Date getBirthday() {
				return oUser.getBirthday();
			}

			@Override
			public String getDescription() {
				return oUser.getDescription();
			}

			@Override
			public ID getOrgId() {
				final IDepartmentService service = orgContext.getDepartmentService();
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
				final Department org = orgContext.getDepartmentService().getBean(getOrgId());
				return org != null ? org.toString() : null;
			}

			@Override
			public InputStream getPhotoStream() {
				return orgContext.getUserService().getPhoto(oUser);
			}

			@Override
			public ID getRoleId() {
				return orgContext.getRoleService().getPrimaryRole(oUser).getId();
			}

			private final IRoleService rService = orgContext.getRoleService();

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
		final IRoleService service = orgContext.getRoleService();
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
				return orgContext.getRoleService().toUniqueName(oRole);
			}

			@Override
			public String getText() {
				return oRole.getText();
			}

			private static final long serialVersionUID = 4548851646225261207L;
		};
	}

	@Override
	public Iterator<ID> users(final Object role, final Map<String, Object> variables) {
		return new NestIterator<ID, User>(orgContext.getRoleService().users(getRoleObject(role),
				variables)) {
			@Override
			protected ID change(final User n) {
				return n.getId();
			}
		};
	}

	@Override
	public Iterator<ID> roles(final Object user, final Map<String, Object> variables) {
		return new NestIterator<ID, Role>(orgContext.getRoleService().roles(getUserObject(user),
				variables)) {
			@Override
			protected ID change(final Role n) {
				return n.getId();
			}
		};
	}

	@Override
	public ID getLoginId(final PageRequestResponse rRequest) {
		return orgContext.getAccountService().getLoginId(new HttpAccountSession(rRequest));
	}

	public static final String ACCOUNT_TYPE = "accountType";

	@Override
	public void login(final PageRequestResponse rRequest, final String login, final String password,
			final Map<String, Object> params) {
		final HttpAccountSession accountSession = new HttpAccountSession(rRequest);
		final IAccountService service = orgContext.getAccountService();

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
			throw OrganizationException.of($m("OrganizationPermission.1")).setCode(2001);
		} else {
			final ID loginId = service.getLoginId(accountSession);
			if (loginId != null && loginId.equals(account.getId())) {
				throw OrganizationException.of($m("OrganizationPermission.0")).setCode(2002);
			}
			// 密码为空时不做校验
			if (password != null && !service.verifyPassword(account, password)) {
				throw OrganizationException.of($m("OrganizationPermission.2"))
						.putVal("password", Boolean.TRUE).setCode(2003);
			} else {
				final EAccountStatus status = account.getStatus();
				if (status == EAccountStatus.normal) {
					service.setLogin(accountSession, new LoginObject(account.getId())
							.setDescription($m("OrganizationPermissionHandler.0")));
				} else if (status == EAccountStatus.locked) {
					throw OrganizationException.of($m("OrganizationPermission.3")).setCode(2004);
				} else if (status == EAccountStatus.registration) {
					throw OrganizationException.of($m("OrganizationPermission.4")).setCode(2005);
				} else if (status == EAccountStatus.delete) {
					throw OrganizationException.of($m("OrganizationPermission.5")).setCode(2006);
				}
			}
		}
	}

	@Override
	public void logout(final PageRequestResponse rRequest) {
		orgContext.getAccountService().logout(new HttpAccountSession(rRequest), true);
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
		orgContext.getAccountService().setLogin(accountSession, loginObject);
	}

	@Override
	protected String getLoginWindowRedirectUrl(final PageRequestResponse rRequest) {
		return AbstractMVCPage.url(LoginWindowRedirect.class);
	}
}
