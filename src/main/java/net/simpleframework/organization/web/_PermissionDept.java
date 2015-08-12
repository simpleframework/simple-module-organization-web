package net.simpleframework.organization.web;

import java.util.ArrayList;
import java.util.List;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.organization.AccountStat;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class _PermissionDept extends PermissionDept implements IOrganizationContextAware {
	private final Department oDept;

	public _PermissionDept(final Department oDept) {
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
		final Department dept = _deptService.getBean(oDept.getParentId());
		return dept == null ? super.getParent() : new _PermissionDept(dept);
	}

	@Override
	public boolean hasChild() {
		return _deptService.hasChild(oDept);
	}

	@Override
	public List<PermissionDept> getChildren() {
		final List<PermissionDept> l = new ArrayList<PermissionDept>();
		final IDataQuery<Department> dq = _deptService.queryChildren(oDept);
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
			final Department org = _deptService.getOrg(_deptService.getBean(getId()));
			if (org != null) {
				setDomainId(domainId = org.getId());
			}
		}
		return domainId;
	}

	@Override
	public boolean isOrg() {
		return oDept.getDepartmentType() == EDepartmentType.organization;
	}

	public Department getDepartment() {
		return oDept;
	}

	@Override
	public String getDomainText() {
		final Department org = _deptService.getBean(getDomainId());
		return org != null ? org.getText() : super.getDomainText();
	}

	private static final long serialVersionUID = 3406269517390528431L;
}
