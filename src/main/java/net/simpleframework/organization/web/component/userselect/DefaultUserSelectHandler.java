package net.simpleframework.organization.web.component.userselect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.ctx.permission.DepartmentWrapper;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.userselect.IUserSelectHandler;
import net.simpleframework.mvc.component.ui.dictionary.AbstractDictionaryHandler;
import net.simpleframework.organization.IDepartment;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IUser;
import net.simpleframework.organization.impl.Department;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class DefaultUserSelectHandler extends AbstractDictionaryHandler implements
		IUserSelectHandler, IOrganizationContextAware {

	@Override
	public IDataQuery<?> getUsers(final ComponentParameter cp) {
		return context.getUserService().queryAll();
	}

	@Override
	public Collection<Object> doSort(final ComponentParameter cp, final Set<Object> groups) {
		final ArrayList<Object> _groups = new ArrayList<Object>(groups);
		final IDepartmentService service = context.getDepartmentService();
		Collections.sort(_groups, new Comparator<Object>() {
			@Override
			public int compare(final Object o1, final Object o2) {
				if (!(o1 instanceof Department)) {
					return 1;
				}
				if (!(o2 instanceof Department)) {
					return -1;
				}
				final Department d1 = (Department) o1;
				final Department d2 = (Department) o2;
				final int l1 = service.getLevel(d1);
				final int l2 = service.getLevel(d2);
				if (l1 == l2) {
					return (int) (d2.getOorder() - d1.getOorder());
				} else {
					return l1 - l2;
				}
			}
		});
		return _groups;
	}

	@Override
	public Object getDepartment(final Object key) {
		final IDepartmentService service = context.getDepartmentService();
		if (key instanceof IDepartment) {
			return key;
		} else if (key instanceof IUser) {
			return service.getBean(((IUser) key).getDepartmentId());
		} else {
			return service.getBean(key);
		}
	}

	@Override
	public Collection<DepartmentWrapper> getDepartmentWrappers(final ComponentParameter cp) {
		final Map<ID, Collection<IDepartment>> depts = context.getDepartmentService().queryAllTree();
		final Map<ID, Collection<IUser>> users = new HashMap<ID, Collection<IUser>>();
		final IDataQuery<?> dq = getUsers(cp).setFetchSize(0);
		IUser user;
		while ((user = (IUser) dq.next()) != null) {
			ID deptId = user.getDepartmentId();
			if (deptId == null) {
				deptId = ID.NULL_ID;
			}
			Collection<IUser> l = users.get(deptId);
			if (l == null) {
				users.put(deptId, l = new ArrayList<IUser>());
			}
			l.add(user);
		}
		return createDepartmentColl(depts, users, depts.get(ID.NULL_ID));
	}

	@SuppressWarnings("unchecked")
	private Collection<DepartmentWrapper> createDepartmentColl(
			final Map<ID, Collection<IDepartment>> depts, final Map<ID, Collection<IUser>> users,
			final Collection<IDepartment> children) {
		final Collection<DepartmentWrapper> wrappers = new ArrayList<DepartmentWrapper>();
		for (final IDepartment dept : children) {
			final DepartmentWrapper wrapper = new DepartmentWrapper(dept);
			final ID k = dept.getId();
			final Collection<IDepartment> v1 = depts.get(k);
			final Collection<IUser> v2 = users.get(k);
			if (v1 != null) {
				wrapper.getChildren().addAll(createDepartmentColl(depts, users, v1));
			}
			if (v2 != null) {
				wrapper.getUsers().addAll(v2);
			}
			wrappers.add(wrapper);
		}
		return wrappers;
	}
}
