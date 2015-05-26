package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.CollectionUtils.NestIterator;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.ctx.permission.PermissionRole;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.ctx.permission.DefaultPagePermissionHandler;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.AccountStat;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EAccountStatus;
import net.simpleframework.organization.EAccountType;
import net.simpleframework.organization.IAccountService;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.IUserService;
import net.simpleframework.organization.LoginObject;
import net.simpleframework.organization.OrganizationException;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RolenameW;
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
		final IRoleService rService = orgContext.getRoleService();
		return new PermissionUser() {
			@Override
			public ID getId() {
				return oUser.getId();
			}

			@Override
			public String getName() {
				final Account account = orgContext.getUserService().getAccount(oUser.getId());
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
			public PermissionDept getDept() {
				PermissionDept dept = super.getDept();
				if (dept == null || dept.getId() == null) {
					setDept(dept = OrganizationPermissionHandler.this.getDept(oUser.getDepartmentId()));
				}
				return dept;
			}

			@Override
			public InputStream getPhotoStream() {
				return orgContext.getUserService().getPhoto(oUser);
			}

			@Override
			public ID getRoleId() {
				return orgContext.getRoleService().getPrimaryRole(oUser).getId();
			}

			private final Map<String, Boolean> _MEMBERs = new ConcurrentHashMap<String, Boolean>();

			@Override
			public boolean isMember(final Object role, final Map<String, Object> variables) {
				String[] arr;
				if (role instanceof String && (arr = StringUtils.split((String) role, ";")).length > 1) {
					for (final String r : arr) {
						if (isMember(r, variables)) {
							return true;
						}
					}
				}
				if (role == null) {
					return rService.isMember(oUser, (Role) null, variables);
				}
				final String rkey = Convert.toString(role);
				Boolean b = _MEMBERs.get(rkey);
				if (b == null) {
					_MEMBERs.put(rkey,
							b = rService.isMember(oUser, getRoleObject(role, variables), variables));
				}
				return b;
			}

			private Boolean _MANAGER;

			@Override
			public boolean isManager(final Map<String, Object> variables) {
				if (_MANAGER == null) {
					_MANAGER = rService.isManager(oUser, variables);
				}
				return _MANAGER;
			}

			private static final long serialVersionUID = -2824016565752293671L;
		};
	}

	protected Role getRoleObject(final Object role, final Map<String, Object> variables) {
		if (role instanceof Role) {
			return (Role) role;
		}
		final IRoleService rService = orgContext.getRoleService();
		if (role instanceof String) {
			Role r = rService.getRoleByName((String) role);
			String[] arr;
			Object userId;
			if (r == null
					&& (variables != null && (userId = variables.get(PermissionConst.VAR_USERID)) != null)
					&& (arr = RolenameW.split((String) role)).length == 2) {
				final User user = orgContext.getUserService().getBean(userId);
				if (user != null) {
					final Department org = orgContext.getDepartmentService().getBean(user.getOrgId());
					r = rService.getRoleByName(
							orgContext.getRoleChartService().getRoleChartByName(org, arr[0]), arr[1]);
				}
			}
			return r;
		}
		return rService.getBean(role);
	}

	@Override
	public PermissionRole getRole(final Object role, final Map<String, Object> variables) {
		final Role oRole = getRoleObject(role, variables);
		if (oRole == null) {
			return super.getRole(role, variables);
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
	public Iterator<ID> users(final Object role, final ID deptId, final Map<String, Object> variables) {
		return new NestIterator<ID, User>(orgContext.getRoleService().users(
				getRoleObject(role, variables), deptId, variables)) {
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

	protected Department getDepartmentObject(final Object dept) {
		if (dept instanceof Department) {
			return (Department) dept;
		}
		final IDepartmentService service = orgContext.getDepartmentService();
		if (dept instanceof String) {
			return service.getDepartmentByName((String) dept);
		}
		return service.getBean(dept);
	}

	@Override
	public PermissionDept getDept(final Object dept) {
		final Department oDept = getDepartmentObject(dept);
		if (oDept == null) {
			return super.getDept(dept);
		}
		return new _PermissionDept(oDept);
	}

	class _PermissionDept extends PermissionDept {
		final IDepartmentService dService = orgContext.getDepartmentService();

		private final Department oDept;

		_PermissionDept(final Department oDept) {
			this.oDept = oDept;
		}

		@Override
		public ID getId() {
			return oDept.getId();
		}

		@Override
		public String getName() {
			return oDept.getName();
		}

		@Override
		public String getText() {
			return oDept.getText();
		}

		@Override
		public int getUsers() {
			final AccountStat stat = orgContext.getAccountStatService().getOrgAccountStat(getId());
			return stat.getNums() - stat.getState_delete();
		}

		@Override
		public PermissionDept getParent() {
			final Department dept = dService.getBean(oDept.getParentId());
			return dept == null ? super.getParent() : new _PermissionDept(dept);
		}

		@Override
		public boolean hasChild() {
			return dService.hasChild(oDept);
		}

		@Override
		public List<PermissionDept> getChildren() {
			final List<PermissionDept> l = new ArrayList<PermissionDept>();
			final IDataQuery<Department> dq = dService.queryChildren(oDept);
			Department dept;
			while ((dept = dq.next()) != null) {
				l.add(new _PermissionDept(dept));
			}
			return l;
		}

		@Override
		public ID getDomainId() {
			ID domainId = super.getDomainId();
			if (domainId == null) {
				final Department org = dService.getOrg(dService.getBean(getId()));
				if (org != null) {
					setDomainId(domainId = org.getId());
				}
			}
			return domainId;
		}

		@Override
		public String getDomainText() {
			final Department org = dService.getBean(getDomainId());
			return org != null ? org.getText() : super.getDomainText();
		}

		private static final long serialVersionUID = 3406269517390528431L;
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
