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
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.userselect.DepartmentW;
import net.simpleframework.mvc.component.ext.userselect.IUserSelectHandler;
import net.simpleframework.mvc.component.ui.dictionary.AbstractDictionaryHandler;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultUserSelectHandler extends AbstractDictionaryHandler implements
		IUserSelectHandler, IOrganizationContextAware {

	@Override
	public IDataQuery<?> getUsers(final ComponentParameter cp) {
		return orgContext.getUserService().queryAll();
	}

	@Override
	public Collection<Object> doSort(final ComponentParameter cp, final Set<Object> groups) {
		final ArrayList<Object> _groups = new ArrayList<Object>(groups);
		final IDepartmentService service = orgContext.getDepartmentService();
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
					return d2.getOorder() - d1.getOorder();
				} else {
					return l1 - l2;
				}
			}
		});
		return _groups;
	}

	@Override
	public Object getDepartment(final Object key) {
		final IDepartmentService service = orgContext.getDepartmentService();
		if (key instanceof Department) {
			return key;
		} else if (key instanceof User) {
			return service.getBean(((User) key).getDepartmentId());
		} else {
			return service.getBean(key);
		}
	}

	@Override
	public Collection<DepartmentW> getDepartmentWrappers(final ComponentParameter cp) {
		final Map<ID, Collection<Department>> depts = orgContext.getDepartmentService()
				.queryAllTree();
		final Map<ID, Collection<User>> users = new HashMap<ID, Collection<User>>();
		final IDataQuery<?> dq = getUsers(cp);
		if (dq != null) {
			dq.setFetchSize(0);
			User user;
			while ((user = (User) dq.next()) != null) {
				ID deptId = user.getDepartmentId();
				if (deptId == null) {
					deptId = ID.NULL_ID;
				}
				Collection<User> l = users.get(deptId);
				if (l == null) {
					users.put(deptId, l = new ArrayList<User>());
				}
				l.add(user);
			}
		}
		return createDepartmentColl(depts, users, depts.get(ID.NULL_ID));
	}

	@SuppressWarnings("unchecked")
	private Collection<DepartmentW> createDepartmentColl(
			final Map<ID, Collection<Department>> depts, final Map<ID, Collection<User>> users,
			final Collection<Department> children) {
		final Collection<DepartmentW> wrappers = new ArrayList<DepartmentW>();
		if (children != null) {
			for (final Department dept : children) {
				final DepartmentW wrapper = new DepartmentW(dept);
				final ID k = dept.getId();
				final Collection<Department> v1 = depts.get(k);
				final Collection<User> v2 = users.get(k);
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
