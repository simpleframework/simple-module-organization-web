package net.simpleframework.organization.web;

import static net.simpleframework.common.I18n.$m;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.CollectionUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.coll.NestIterator;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.ctx.permission.PermissionRole;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.MVCConst;
import net.simpleframework.mvc.PageRequestResponse;
import net.simpleframework.mvc.ctx.permission.DefaultPagePermissionHandler;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.Account.EAccountStatus;
import net.simpleframework.organization.Account.EAccountType;
import net.simpleframework.organization.AccountStat;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.Department.EDepartmentType;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleService.RoleM;
import net.simpleframework.organization.OrganizationException;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.RoleMember;
import net.simpleframework.organization.RoleMember.ERoleMemberType;
import net.simpleframework.organization.User;
import net.simpleframework.organization.login.LoginObject;
import net.simpleframework.organization.role.RolenameW;
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
		if (o instanceof String) {
			final String s = (String) o;
			if (s.contains("@")) {
				return _userService.getUserByEmail(s);
			} else {
				final Account account = _accountService.getAccountByName(s);
				if (account != null) {
					o = account;
				}
			}
		}
		if (o instanceof Account) {
			o = ((Account) o).getId();
		}
		return _accountService.getUser(o);
	}

	protected Role getRoleObject(final Object role, final Map<String, Object> variables) {
		if (role instanceof Role) {
			return (Role) role;
		}
		if (role instanceof String) {
			Role r = _roleService.getRoleByName((String) role);
			String[] arr;
			Object userId;
			if (r == null
					&& (variables != null && (userId = variables.get(PermissionConst.VAR_USERID)) != null)
					&& (arr = RolenameW.split((String) role)).length == 2) {
				final User user = _userService.getBean(userId);
				if (user != null) {
					final Department org = _deptService.getBean(user.getOrgId());
					r = _roleService
							.getRoleByName(_rolecService.getRoleChartByName(org, arr[0]), arr[1]);
				}
			}
			return r;
		}
		return _roleService.getBean(role);
	}

	protected Department getDepartmentObject(final Object dept) {
		if (dept instanceof Department) {
			return (Department) dept;
		}
		if (dept instanceof String) {
			return _deptService.getDepartmentByName((String) dept);
		}
		return _deptService.getBean(dept);
	}

	@Override
	public ID getLoginId(final PageRequestResponse rRequest) {
		return _accountService.getLoginId(new HttpAccountSession(rRequest));
	}

	public static final String ACCOUNT_TYPE = "accountType";

	@Override
	public void login(final PageRequestResponse rRequest, final String login, final String password,
			final Map<String, Object> params) {
		final HttpAccountSession accountSession = new HttpAccountSession(rRequest);

		EAccountType accountType = null;
		if (params != null) {
			accountType = (EAccountType) params.get(ACCOUNT_TYPE);
		}
		if (accountType == null) {
			accountType = EAccountType.normal;
		}

		Account account = null;
		if (accountType == EAccountType.normal) {
			account = _accountService.getAccountByName(login);
		} else if (accountType == EAccountType.email) {
			final User user = _userService.getUserByEmail(login);
			if (user != null) {
				account = _accountService.getBean(user.getId());
				if (account != null && !account.isMailbinding()) {
					throw OrganizationException.of($m("OrganizationPermission.6"));
				}
			}
		} else if (accountType == EAccountType.mobile) {
			final User user = _userService.getUserByMobile(login);
			if (user != null) {
				account = _accountService.getBean(user.getId());
				if (account != null && !account.isMobilebinding()) {
					throw OrganizationException.of($m("OrganizationPermission.7"));
				}
			}
		}

		if (account == null) {
			throw OrganizationException.of($m("OrganizationPermission.1")).setCode(2001);
		} else {
			final ID loginId = _accountService.getLoginId(accountSession);
			if (loginId != null && loginId.equals(account.getId())) {
				throw OrganizationException.of($m("OrganizationPermission.0")).setCode(2002);
			}
			// 密码为空时不做校验
			if (password != null && !_accountService.verifyPassword(account, password)) {
				throw OrganizationException.of($m("OrganizationPermission.2"))
						.putVal("password", Boolean.TRUE).setCode(2003);
			} else {
				final EAccountStatus status = account.getStatus();
				if (status == EAccountStatus.normal) {
					_accountService.setLogin(accountSession, new LoginObject(account.getId())
							.setDescription($m("OrganizationPermissionHandler.0")));
					rRequest.removeRequestAttr("_getLogin");
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
		_accountService.logout(new HttpAccountSession(rRequest), true);
		rRequest.removeSessionAttr(MVCConst.SESSION_ATTRI_LASTURL);
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
		_accountService.setLogin(accountSession, loginObject);
	}

	@Override
	protected String getLoginWindowRedirectUrl(final PageRequestResponse rRequest) {
		return AbstractMVCPage.url(LoginWindowRedirect.class);
	}

	@Override
	public Iterator<PermissionUser> allUsers() {
		return new NestIterator<PermissionUser, User>(DataQueryUtils.toIterator(_userService
				.queryAll())) {
			@Override
			protected PermissionUser change(final User n) {
				return OrganizationPermissionHandler.this.getUser(n);
			}
		};
	}

	@Override
	public PermissionUser getUser(final Object user) {
		final User _user = getUserObject(user);
		return _user == null ? super.getUser(user) : new _PermissionUser(_user);
	}

	protected class _PermissionUser extends PermissionUser {
		private final User oUser;

		protected _PermissionUser(final User oUser) {
			this.oUser = oUser;
		}

		@Override
		public ID getId() {
			return oUser.getId();
		}

		@Override
		public String getName() {
			final Account account = _userService.getAccount(getId());
			return account != null ? account.getName() : super.getName();
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
		public String getMobile() {
			return oUser.getMobile();
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
		public InputStream getPhotoStream() {
			return _userService.getPhoto(oUser);
		}

		@Override
		public int getOorder() {
			return oUser.getOorder();
		}

		@Override
		public PermissionRole getRole() {
			PermissionRole _role = super.getRole();
			if (_role.getId() == null) {
				// 获取缺省角色
				setRole(_role = OrganizationPermissionHandler.this.getRole(_roleService.getPrimaryRole(
						oUser).getId()));
			}
			return _role.setUser(this);
		}

		@Override
		public PermissionDept getDept() {
			PermissionDept _dept = super.getDept();
			if (_dept.getId() == null) {
				// 从用户实体部门属性获取
				setDept(_dept = OrganizationPermissionHandler.this.getDept(oUser.getDepartmentId()));
			}
			return _dept;
		}

		private final Map<String, Boolean> _MEMBERs = new ConcurrentHashMap<String, Boolean>();

		@Override
		public boolean isMember(final Object role, final Map<String, Object> variables) {
			variables.put(PermissionConst.VAR_USERID, this.getId());

			String[] arr;
			if (role instanceof String && (arr = StringUtils.split((String) role, ";")).length > 1) {
				for (final String r : arr) {
					if (isMember(r, variables)) {
						return true;
					}
				}
			}

			if (role == null) {
				return _roleService.isMember(oUser, (Role) null, variables);
			}

			// 加入缓存
			String rkey = Convert.toString(role);
			final Object deptId = variables.get(PermissionConst.VAR_DEPTID);
			if (deptId != null) {
				rkey += ":" + deptId;
			}
			Boolean b = _MEMBERs.get(rkey);
			if (b == null) {
				_MEMBERs.put(rkey,
						b = _roleService.isMember(oUser, getRoleObject(role, variables), variables));
			}
			return b;
		}

		private Boolean _MANAGER;

		@Override
		public boolean isManager(final Map<String, Object> variables) {
			if (_MANAGER == null) {
				variables.put(PermissionConst.VAR_USERID, this.getId());
				_MANAGER = _roleService.isManager(oUser, variables);
			}
			return _MANAGER;
		}

		@Override
		public List<PermissionRole> roles(final Map<String, Object> variables) {
			final ArrayList<PermissionRole> l = new ArrayList<PermissionRole>();
			final Iterator<RoleM> it = _roleService.roles(oUser, variables);
			final OrganizationPermissionHandler hdl = OrganizationPermissionHandler.this;
			RoleM n;
			while (it.hasNext()) {
				n = it.next();
				final PermissionRole r = hdl.getRole(n.role).setUser(_PermissionUser.this);
				l.add(r.setDept(hdl.getDept(n.rm.getDeptId())));
			}
			return l;
		}

		private static final long serialVersionUID = -2824016565752293671L;
	}

	@Override
	public PermissionRole getRole(final Object role, final Map<String, Object> variables) {
		final Role _role = getRoleObject(role, variables);
		return _role == null ? super.getRole(role, variables) : new _PermissionRole(_role);
	}

	protected class _PermissionRole extends PermissionRole {
		private final Role oRole;

		protected _PermissionRole(final Role oRole) {
			this.oRole = oRole;
		}

		@Override
		public ID getId() {
			return oRole.getId();
		}

		@Override
		public String getName() {
			return _roleService.toUniqueName(oRole);
		}

		@Override
		public String getText() {
			return oRole.getText();
		}

		@Override
		public Iterator<PermissionUser> users(final ID deptId, final Map<String, Object> variables) {
			if (deptId != null) {
				variables.put(PermissionConst.VAR_DEPTID, deptId);
			}
			return new NestIterator<PermissionUser, User>(_roleService.users(oRole, variables)) {
				@Override
				protected PermissionUser change(final User n) {
					final OrganizationPermissionHandler hdl = OrganizationPermissionHandler.this;
					final PermissionUser user = hdl.getUser(n.getId()).setRole(_PermissionRole.this);
					if (deptId != null) {
						user.setDept(hdl.getDept(deptId));
					}
					return user;
				}
			};
		}

		@Override
		public PermissionUser getUser() {
			return super.getUser().setRole(this);
		}

		@Override
		public PermissionDept getDept() {
			PermissionDept _dept = super.getDept();
			if (_dept.getId() == null) {
				final PermissionUser user = getUser();
				final ID userId = user.getId();
				if (userId != null) {
					final OrganizationPermissionHandler hdl = OrganizationPermissionHandler.this;
					final RoleMember rm;
					if ((rm = _rolemService.getBean("roleId=? and memberType=? and memberId=?", getId(),
							ERoleMemberType.user, userId)) != null) {
						_dept = hdl.getDept(rm.getDeptId());
					}
					if (_dept.getId() == null) {
						_dept = hdl.getDept(user.getDept().getId());
					}
					setDept(_dept);
				}
			}
			return _dept;
		}

		private static final long serialVersionUID = 4548851646225261207L;
	}

	@Override
	public PermissionDept getDept(final Object dept) {
		final Department _dept = getDepartmentObject(dept);
		return _dept == null ? super.getDept(dept) : new _PermissionDept(_dept);
	}

	@Override
	public List<PermissionDept> getRootChildren() {
		return _dept_children(_deptService.queryChildren(null));
	}

	private List<PermissionDept> _dept_children(final IDataQuery<Department> dq) {
		final List<PermissionDept> l = new ArrayList<PermissionDept>();
		Department dept;
		while ((dept = dq.next()) != null) {
			l.add(getDept(dept));
		}
		return l;
	}

	protected class _PermissionDept extends PermissionDept {
		private final Department oDept;

		protected _PermissionDept(final Department oDept) {
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
		public int getLevel() {
			return _deptService.getLevel(oDept);
		}

		@Override
		public int getOorder() {
			return oDept.getOorder();
		}

		@Override
		public int getUserCount() {
			final AccountStat stat = _accountStatService.getOrgAccountStat(getId());
			return stat != null ? stat.getRnums() : 0;
		}

		@Override
		public Iterator<PermissionUser> users(final boolean rolemember) {
			final Iterator<User> it = _roleService.users(getDepartmentObject(oDept),
					new KVMap().add("role-member", rolemember));
			return new NestIterator<PermissionUser, User>(it) {
				@Override
				protected PermissionUser change(final User n) {
					return OrganizationPermissionHandler.this.getUser(n);
				}
			};
		}

		@Override
		public Iterator<PermissionUser> orgUsers() {
			return new NestIterator<PermissionUser, User>(DataQueryUtils.toIterator(_userService
					.queryUsers(getDepartmentObject(oDept), Account.TYPE_ALL))) {
				@Override
				protected PermissionUser change(final User n) {
					return OrganizationPermissionHandler.this.getUser(n);
				}
			};
		}

		@Override
		public List<PermissionDept> getAllChildren() {
			return _dept_children(_deptService.queryChildren(oDept));
		}

		@Override
		public List<PermissionDept> getChildren() {
			return _dept_children(_deptService.queryDepartments(oDept, EDepartmentType.department));
		}

		@Override
		public List<PermissionDept> getOrgChildren() {
			if (isOrg()) {
				return _dept_children(_deptService
						.queryDepartments(oDept, EDepartmentType.organization));
			} else {
				return CollectionUtils.EMPTY_LIST();
			}
		}

		@Override
		public ID getParentId() {
			return oDept.getParentId();
		}

		private Department getOrg() {
			return _deptService.getOrg(_deptService.getBean(getId()));
		}

		@Override
		public ID getDomainId() {
			final Department org = getOrg();
			return org != null ? org.getId() : null;
		}

		@Override
		public boolean isOrg() {
			return oDept.getDepartmentType() == EDepartmentType.organization;
		}

		@Override
		public String getDomainText() {
			final Department org = getOrg();
			return org != null ? org.getText() : super.getDomainText();
		}

		private static final long serialVersionUID = 3406269517390528431L;
	}
}
