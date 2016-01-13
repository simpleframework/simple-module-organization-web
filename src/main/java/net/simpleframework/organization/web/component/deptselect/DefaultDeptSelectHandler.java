package net.simpleframework.organization.web.component.deptselect;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.deptselect.DeptSelectBean;
import net.simpleframework.mvc.component.ext.deptselect.IDeptSelectHandle;
import net.simpleframework.mvc.component.ui.dictionary.AbstractDictionaryHandler;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean.DictionaryTreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.ctx.permission.IPagePermissionHandler;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.Department.EDepartmentType;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultDeptSelectHandler extends AbstractDictionaryHandler implements
		IDeptSelectHandle, IOrganizationContextAware {

	@Override
	public Collection<PermissionDept> getDepartments(final ComponentParameter cp,
			final TreeBean treeBean, final PermissionDept parent) {
		final IPagePermissionHandler permission = cp.getPermission();
		final Department pdept = parent != null ? _deptService.getBean(parent.getId()) : null;
		IDataQuery<Department> dq = null;
		// 仅显示机构
		final boolean borg = (Boolean) cp.getBeanProperty("org");
		if (borg) {
			dq = _deptService.queryDepartments(pdept, EDepartmentType.organization);
		} else {
			final PermissionDept org = AbstractMVCPage.getPermissionOrg(cp);
			if (org.exists()) {
				// 单机构
				if (parent == null) {
					return Arrays.asList(org);
				} else {
					dq = _deptService.queryDepartments(pdept, EDepartmentType.department);
				}
			} else {
				dq = _deptService.queryDepartments(pdept, null);
			}
		}

		final List<PermissionDept> al = new ArrayList<PermissionDept>();
		Department dept;
		while ((dept = dq.next()) != null) {
			al.add(permission.getDept(dept));
		}
		return al;
	}

	@Override
	protected ElementList getRightElements(final ComponentParameter cp) {
		if (Convert.toBool(cp.getBeanProperty("multiple"))) {
			final String componentName = ((DictionaryTreeBean) ((DeptSelectBean) cp.componentBean)
					.getDictionaryTypeBean()).getRef();
			return ElementList.of(new Checkbox("idDefaultDeptSelectHandler_cb",
					$m("DefaultDeptSelectHandler.0")).setOnclick("$Actions['" + componentName
					+ "'].checkAll(this.checked);"));
		}
		return null;
	}
}
