package net.simpleframework.organization.web.component.userselect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.userselect.AbstractUserSelectHandler;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultUserSelectHandler extends AbstractUserSelectHandler implements
		IOrganizationContextAware {

	// private final Map<ID, List<PermissionDept>> allDepts = new HashMap<ID,
	// List<PermissionDept>>();

	@Override
	public Collection<DepartmentMemory> getDepartments(final ComponentParameter cp) {
		// cp.getPermission().getRootChildren();
		final Map<ID, Collection<Department>> dTreemap = DataQueryUtils.toTreeMap(_deptService
				.queryAll());
		final Map<ID, Collection<PermissionUser>> users = new HashMap<ID, Collection<PermissionUser>>();
		final IDataQuery<?> dq = getUsers(cp);
		if (dq != null) {
			dq.setFetchSize(0);
			PermissionUser user;
			while ((user = (PermissionUser) dq.next()) != null) {
				final PermissionDept dept = user.getDept();
				final ID deptId = dept.exists() ? dept.getId() : ID.NULL_ID;
				Collection<PermissionUser> l = users.get(deptId);
				if (l == null) {
					users.put(deptId, l = new ArrayList<PermissionUser>());
				}
				l.add(user);
			}
		}
		return createDepartmentColl(dTreemap, users, dTreemap.get(ID.NULL_ID));
	}

	@SuppressWarnings("unchecked")
	private Collection<DepartmentMemory> createDepartmentColl(
			final Map<ID, Collection<Department>> depts,
			final Map<ID, Collection<PermissionUser>> users, final Collection<Department> children) {
		final Collection<DepartmentMemory> wrappers = new ArrayList<DepartmentMemory>();
		if (children != null) {
			for (final Department dept : children) {
				final DepartmentMemory wrapper = new DepartmentMemory(dept);
				final ID k = dept.getId();
				final Collection<Department> v1 = depts.get(k);
				final Collection<PermissionUser> v2 = users.get(k);
				if (v1 != null) {
					wrapper.getChildren().addAll(createDepartmentColl(depts, users, v1));
				}
				if (v2 != null) {
					wrapper.getUsers().addAll(v2);
				}
				wrappers.add(wrapper);
			}
		}
		return wrappers;
	}
}
